package com.example.studmate.data.repo

import com.example.studmate.data.local.ClassDao
import com.example.studmate.data.local.CommunityDao
import com.example.studmate.data.local.EnrollmentDao
import com.example.studmate.data.local.ReviewEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.data.local.UserDao
import com.example.studmate.data.local.UserEntity
import com.example.studmate.domain.ClassStatus
import com.example.studmate.domain.ClassSummary
import com.example.studmate.domain.MentorStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

data class ReviewItem(val review: ReviewEntity, val user: UserEntity?)

class CatalogRepository(
    private val classes: ClassDao,
    enrollments: EnrollmentDao,
    private val community: CommunityDao,
    private val users: UserDao,
    scope: CoroutineScope,
) {

    val allSummaries: Flow<List<ClassSummary>> = combine(
        classes.observeAll(), users.observeAll(), enrollments.observeCounts(),
        community.observeRatings(), classes.observeSessionAgg(),
    ) { cls, us, counts, ratings, aggs ->
        val userMap = us.associateBy { it.id }
        val countMap = counts.associate { it.classId to it.count }
        val ratingMap = ratings.associateBy { it.classId }
        val aggMap = aggs.associateBy { it.classId }
        cls.map { c ->
            ClassSummary(
                cls = c,
                organizer = userMap[c.organizerId],
                enrolled = countMap[c.id] ?: 0,
                rating = ratingMap[c.id]?.avg ?: 0.0,
                reviewCount = ratingMap[c.id]?.cnt ?: 0,
                sessionCount = aggMap[c.id]?.cnt ?: 0,
                totalMinutes = aggMap[c.id]?.minutes ?: 0,
            )
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(5_000), replay = 1)


    val published: Flow<List<ClassSummary>> = allSummaries.map { list ->
        list.filter { it.cls.status == ClassStatus.PUBLISHED }
    }

    val mentors: Flow<List<MentorStats>> = combine(allSummaries, users.observeAll()) { sums, us ->
        val byOrg = sums.filter { it.cls.status != ClassStatus.DRAFT }.groupBy { it.cls.organizerId }
        us.mapNotNull { u ->
            val list = byOrg[u.id] ?: return@mapNotNull null
            val reviewCount = list.sumOf { it.reviewCount }
            MentorStats(
                user = u,
                classCount = list.size,
                studentCount = list.sumOf { it.enrolled },
                avgRating = if (reviewCount == 0) 0.0 else list.sumOf { it.rating * it.reviewCount } / reviewCount,
                reviewCount = reviewCount,
                firstClassAt = list.minOfOrNull { it.cls.createdAt },
            )
        }
    }

    fun summary(id: String): Flow<ClassSummary?> = allSummaries.map { l -> l.find { it.cls.id == id } }
    fun sessions(classId: String): Flow<List<SessionEntity>> = classes.observeSessions(classId)
    val allSessions: Flow<List<SessionEntity>> = classes.observeAllSessions()

    fun reviews(classId: String): Flow<List<ReviewItem>> =
        combine(community.observeReviews(classId), users.observeAll()) { r, us ->
            val m = us.associateBy { it.id }
            r.map { ReviewItem(it, m[it.userId]) }
        }

    fun mentor(userId: String): Flow<MentorStats?> = combine(mentors, users.observe(userId)) { list, u ->
        list.find { it.user.id == userId } ?: u?.let { MentorStats(it, 0, 0, 0.0, 0, null) }
    }

    fun classesBy(organizerId: String): Flow<List<ClassSummary>> = allSummaries.map { l ->
        l.filter { it.cls.organizerId == organizerId && it.cls.status != ClassStatus.DRAFT }
            .sortedByDescending { it.cls.startAt }
    }
}
