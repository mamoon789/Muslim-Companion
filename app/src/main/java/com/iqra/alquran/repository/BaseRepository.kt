package com.iqra.alquran.repository

import com.iqra.alquran.network.wrapper.Resource
import com.iqra.alquran.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

abstract class BaseRepository
{
    suspend fun <T> safeApiCall(apiToBeCalled: suspend () -> Response<T>): Resource<T>
    {
        return withContext(Dispatchers.IO) {
            try
            {
                val response: Response<T> = apiToBeCalled()
                if (response.isSuccessful)
                {
                    Resource.Success(data = response.body()!!)
                } else
                {
                    val errorResponse = JSONObject(response.errorBody()?.string() ?: "")
                    Resource.Error(
                        errorMessage = if (errorResponse.has("message"))
                        {
                            errorResponse.getString("message") ?: Constants.MSG_TRY_LATER
                        } else
                        {
                            response.message() ?: Constants.MSG_TRY_LATER
                        }
                    )
                }
            } catch (e: HttpException)
            {
                Resource.Error(errorMessage = e.message ?: Constants.MSG_TRY_LATER)
            } catch (e: IOException)
            {
                Resource.Error(errorMessage = Constants.MSG_CONNECT_INTERNET)
            } catch (e: Exception)
            {
                Resource.Error(errorMessage = Constants.MSG_TRY_LATER)
            }
        }
    }
}