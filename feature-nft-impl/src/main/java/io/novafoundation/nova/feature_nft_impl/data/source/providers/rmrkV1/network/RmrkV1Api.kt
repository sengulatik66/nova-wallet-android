package io.novafoundation.nova.feature_nft_impl.data.source.providers.rmrkV1.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface RmrkV1Api {

    companion object {
        const val BASE_URL = "https://singular.rmrk.app/api/rmrk1/"
    }

    @GET("account/{address}")
    suspend fun getNfts(@Path("address") address: String): List<RmrkV1NftRemote>

    @GET("collection/{collectionId}")
    suspend fun getCollection(@Path("collectionId") collectionId: String): List<RmrkV1CollectionRemote>

    @GET
    suspend fun getIpfsMetadata(@Url url: String): RmrkV1NftMetadataRemote
}
