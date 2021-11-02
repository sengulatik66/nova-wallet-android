package io.novafoundation.nova.feature_staking_impl.presentation.payouts.detail

import io.novafoundation.nova.common.address.AddressIconGenerator
import io.novafoundation.nova.common.address.createAddressModel
import io.novafoundation.nova.common.base.BaseViewModel
import io.novafoundation.nova.common.resources.ResourceManager
import io.novafoundation.nova.common.utils.formatAsCurrency
import io.novafoundation.nova.common.utils.inBackground
import io.novafoundation.nova.feature_account_api.presenatation.actions.ExternalActions
import io.novafoundation.nova.feature_staking_impl.R
import io.novafoundation.nova.feature_staking_impl.domain.StakingInteractor
import io.novafoundation.nova.feature_staking_impl.presentation.StakingRouter
import io.novafoundation.nova.feature_staking_impl.presentation.payouts.confirm.model.ConfirmPayoutPayload
import io.novafoundation.nova.feature_staking_impl.presentation.payouts.model.PendingPayoutParcelable
import io.novafoundation.nova.feature_wallet_api.domain.model.Asset
import io.novafoundation.nova.feature_wallet_api.domain.model.amountFromPlanks
import io.novafoundation.nova.feature_wallet_api.presentation.formatters.formatTokenAmount
import io.novafoundation.nova.runtime.state.SingleAssetSharedState
import io.novafoundation.nova.runtime.state.chain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PayoutDetailsViewModel(
    private val interactor: StakingInteractor,
    private val router: StakingRouter,
    private val payout: PendingPayoutParcelable,
    private val addressModelGenerator: AddressIconGenerator,
    private val externalActions: ExternalActions.Presentation,
    private val resourceManager: ResourceManager,
    private val selectedAssetState: SingleAssetSharedState,
) : BaseViewModel(), ExternalActions.Presentation by externalActions {

    val payoutDetails = interactor.currentAssetFlow()
        .map(::mapPayoutParcelableToPayoutDetailsModel)
        .inBackground()
        .asLiveData()

    fun backClicked() {
        router.back()
    }

    fun payoutClicked() {
        val payload = ConfirmPayoutPayload(
            totalRewardInPlanks = payout.amountInPlanks,
            payouts = listOf(payout)
        )

        router.openConfirmPayout(payload)
    }

    fun validatorExternalActionClicked() = launch {
        externalActions.showExternalActions(ExternalActions.Type.Address(payout.validatorInfo.address), selectedAssetState.chain())
    }

    private suspend fun mapPayoutParcelableToPayoutDetailsModel(asset: Asset): PayoutDetailsModel {
        val tokenType = asset.token.configuration
        val rewardAmount = asset.token.amountFromPlanks(payout.amountInPlanks)

        val addressModel = with(payout.validatorInfo) {
            addressModelGenerator.createAddressModel(address, AddressIconGenerator.SIZE_SMALL, identityName)
        }

        return PayoutDetailsModel(
            validatorAddressModel = addressModel,
            createdAt = payout.createdAt,
            eraDisplay = resourceManager.getString(R.string.staking_era_index_no_prefix, payout.era.toLong()),
            reward = rewardAmount.formatTokenAmount(tokenType),
            rewardFiat = asset.token.fiatAmount(rewardAmount).formatAsCurrency()
        )
    }
}