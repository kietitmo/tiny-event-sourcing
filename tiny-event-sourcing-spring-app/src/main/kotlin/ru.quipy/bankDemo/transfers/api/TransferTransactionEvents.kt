package ru.quipy.bankDemo.transfers.api

import ru.quipy.core.annotations.DomainEvent
import ru.quipy.domain.Event
import java.math.BigDecimal
import java.util.*

const val TRANSFER_TRANSACTION_CREATED = "TRANSFER_TRANSACTION_CREATED"
const val TRANSFER_SUCCEEDED = "TRANSFER_SUCCEEDED"
const val TRANSFER_FAILED = "TRANSFER_FAILED"
const val NOOP = "NOOP"
const val TRANSFER_CANCELLED = "TRANSFER_CANCELLED"

@DomainEvent(name = TRANSFER_TRANSACTION_CREATED)
data class TransferTransactionCreatedEvent(
    val transferId: UUID,
    val sourceAccountId: UUID,
    val sourceBankAccountId: UUID,
    val destinationAccountId: UUID,
    val destinationBankAccountId: UUID,
    val transferAmount: BigDecimal,
) : Event<TransferTransactionAggregate>(
    name = TRANSFER_TRANSACTION_CREATED,
)

@DomainEvent(name = NOOP)
data class NoopEvent(
    val transferId: UUID,
) : Event<TransferTransactionAggregate>(
    name = NOOP,
)

@DomainEvent(name = TRANSFER_SUCCEEDED)
data class TransactionSucceededEvent(
    val transferId: UUID,
) : Event<TransferTransactionAggregate>(
    name = TRANSFER_SUCCEEDED,
)

@DomainEvent(name = TRANSFER_FAILED)
data class TransactionFailedEvent(
    val transferId: UUID,
) : Event<TransferTransactionAggregate>(
    name = TRANSFER_FAILED,
)

//@DomainEvent(name = TRANSFER_CANCELLED)
//data class TransactionCancelledEvent(
//    val transferId: UUID,
//    val sourceAccountId: UUID,
//    val sourceBankAccountId: UUID,
//    val destinationAccountId: UUID,
//    val destinationBankAccountId: UUID,
//) : Event<TransferTransactionAggregate>(
//    name = TRANSFER_CANCELLED,
//    createdAt = System.currentTimeMillis(),
//)
