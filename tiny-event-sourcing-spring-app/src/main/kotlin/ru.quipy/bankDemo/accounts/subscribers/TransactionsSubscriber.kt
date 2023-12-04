package ru.quipy.bankDemo.accounts.subscribers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.quipy.bankDemo.accounts.api.AccountAggregate
import ru.quipy.bankDemo.transfers.api.*
import ru.quipy.bankDemo.accounts.logic.Account
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.core.EventSourcingService
import ru.quipy.saga.SagaManager
import java.util.*
import javax.annotation.PostConstruct

@Component
class TransactionsSubscriber(
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val accountEsService: EventSourcingService<UUID, AccountAggregate, Account>,
    private val sagaManager: SagaManager
) {
    private val logger: Logger = LoggerFactory.getLogger(TransactionsSubscriber::class.java)
    val bankSagaName = "BANK_OPERATION"

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(TransferTransactionAggregate::class, "accounts::transaction-processing-subscriber") {
            `when`(TransferTransactionCreatedEvent::class) { event ->
                logger.info("Got transaction to process: $event")
                val sagaContext = sagaManager
                    .withContextGiven(event.sagaContext)
                    .performSagaStep(bankSagaName, "perform transfer")
                    .sagaContext

                val transactionOutcome1 = accountEsService.update(event.sourceAccountId, sagaContext) { // todo sukhoa idempotence!
                    it.performFromAndProcess(
                        event.sourceBankAccountId,
                        event.transferId,
                        event.transferAmount
                    )
                }

                val transactionOutcome2 = accountEsService.update(event.destinationAccountId, sagaContext) { // todo sukhoa idempotence!
                    it.performToAndProcess(
                        event.destinationBankAccountId,
                        event.transferId,
                        event.transferAmount
                    )
                }

                logger.info("Transaction: ${event.transferId}. Outcomes: $transactionOutcome1, $transactionOutcome2")
            }
        }
    }
}