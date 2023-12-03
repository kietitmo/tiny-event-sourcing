package ru.quipy.bankDemo.transfers.subscribers

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.quipy.bankDemo.accounts.api.*
import ru.quipy.bankDemo.transfers.api.TransferTransactionAggregate
import ru.quipy.bankDemo.transfers.api.TransferTransactionCreatedEvent
import ru.quipy.bankDemo.accounts.logic.Account
import ru.quipy.bankDemo.transfers.logic.TransferTransaction
import ru.quipy.core.EventSourcingService
import ru.quipy.streams.AggregateSubscriptionsManager
import ru.quipy.saga.SagaManager
import java.util.*
import javax.annotation.PostConstruct

@Component
class BankAccountsSubscriber(
    private val subscriptionsManager: AggregateSubscriptionsManager,
    private val transactionEsService: EventSourcingService<UUID, TransferTransactionAggregate, TransferTransaction>,
    private val sagaManager: SagaManager
) {
    private val logger: Logger = LoggerFactory.getLogger(BankAccountsSubscriber::class.java)
    val bankSagaName = "BANK_OPERATION"

    @PostConstruct
    fun init() {
        subscriptionsManager.createSubscriber(AccountAggregate::class, "transactions::bank-accounts-subscriber") {
            `when`(TransferTransactionAcceptedEvent::class) { event ->
                val sagaContext = sagaManager
                    .withContextGiven(event.sagaContext)
                    .performSagaStep(bankSagaName, "process ParticipantAccept")
                    .sagaContext

                transactionEsService.update(event.transactionId, sagaContext) {
                    it.processParticipantAccept(event.bankAccountId)
                }
            }

            `when`(TransferTransactionDeclinedEvent::class) { event ->
                val sagaContext = sagaManager
                    .withContextGiven(event.sagaContext)
                    .performSagaStep(bankSagaName, "process ParticipantDecline")
                    .sagaContext

                transactionEsService.update(event.transactionId, sagaContext) {
                    it.processParticipantDecline(event.bankAccountId)
                }
            }

            `when`(TransferTransactionProcessedEvent::class) { event ->
                val sagaContext = sagaManager
                    .withContextGiven(event.sagaContext)
                    .performSagaStep(bankSagaName, "finish transfer")
                    .sagaContext

                transactionEsService.update(event.transactionId, sagaContext) {
                    it.participantCommitted(event.bankAccountId)
                }
            }

            `when`(TransferTransactionRollbackedEvent::class) { event ->
                val sagaContext = sagaManager
                    .withContextGiven(event.sagaContext)
                    .performSagaStep(bankSagaName, "rollback transfer")
                    .sagaContext

                transactionEsService.update(event.transactionId, sagaContext) {
                    it.participantRollbacked(event.bankAccountId)
                }
            }
        }
    }
}