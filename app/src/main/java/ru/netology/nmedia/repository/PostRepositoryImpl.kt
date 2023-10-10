package ru.netology.nmedia.repository

import androidx.lifecycle.*
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntity.Companion.fromDto
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toDto)
    val unsavedPosts = mutableListOf<Post>()


    override suspend fun getAll() {
        try {
            val response = PostsApi.service.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {

                try {
                    val response = PostsApi.service.save(post)
                    if (!response.isSuccessful) {
                        throw ApiError(response.code(), response.message())
                    }

                    val body = response.body() ?: throw ApiError(response.code(), response.message())
                    dao.insert(PostEntity.fromDto(body))
                } catch (e: IOException) {
                    unsavedPosts.add(post)
                    throw NetworkError
                } catch (e: Exception) {
                    unsavedPosts.add(post)
                    throw UnknownError
                }
        }



    override suspend fun removeById(id: Long) {

        val oldPost = data.value.orEmpty().find { it.id == id }

        dao.removeById(id)

        try {
            val response = PostsApi.service.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

        } catch (e: IOException) {
            if (oldPost != null) {
                dao.insert(fromDto(oldPost))
            }
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {

        val post = data.value.orEmpty().find { it.id == id }

        if (post != null) {
            val updatedPost = post.copy(likes = post.likes + 1, likedByMe = true)
            dao.insert(fromDto(updatedPost))
        }

        try {
            val response = PostsApi.service.likeById(id)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(fromDto(body))

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            if (post != null) {
                dao.insert(fromDto(post))
            }
            throw NetworkError

        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun disLikeById(id: Long) {

        val post = data.value.orEmpty().find { it.id == id }

        if (post != null) {
            val updatedPost = post.copy(likes = post.likes - 1, likedByMe = false)
            dao.insert(fromDto(updatedPost))
        }

        try {
            val response = PostsApi.service.dislikeById(id)
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(fromDto(body))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            if (post != null) {
                dao.insert(fromDto(post))
            }
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

//    override suspend fun sendUnsavedPostsToServer() {
//        for (unsavedPost in unsavedPosts) {
//            try {
//                val response = PostsApi.service.save(unsavedPost)
//                if (response.isSuccessful) {
//                    // После успешной отправки на сервер, обновите локальную базу данных
//                    val body = response.body()
//                    if (body != null) {
//                        dao.insert(PostEntity.fromDto(body))
//                    }
//                    // Удалите пост из списка несохраненных, так как он теперь сохранен
//                    unsavedPosts.remove(unsavedPost)
//                }
//            } catch (e: IOException) {
//                // Обработка ошибки сети
//                // Возможно, здесь нужно записать флаг или состояние, показывающее, что пост не был отправлен на сервер
//            } catch (e: Exception) {
//                // Обработка других исключений
//            }
//        }
//    }
}
