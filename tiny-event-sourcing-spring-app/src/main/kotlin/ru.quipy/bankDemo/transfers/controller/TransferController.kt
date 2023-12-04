package ru.quipy.bankDemo.transfers.controller

import org.springframework.web.bind.annotation.*
import ru.quipy.bankDemo.transfers.api.*
import ru.quipy.bankDemo.transfers.logic.*
import ru.quipy.core.EventSourcingService
import ru.quipy.saga.SagaManager
import ru.quipy.bankDemo.transfers.service.TransactionService
import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping("/transfer")
class TransferController(
    val transferService: TransactionService,
) {
    @PostMapping
    fun transfer(
        @RequestParam sourceBankAccountId: UUID,
        @RequestParam destinationBankAccountId: UUID,
        @RequestParam transferAmount: BigDecimal
    ) : TransferTransactionCreatedEvent {
        return transferService.initiateTransferTransaction(sourceBankAccountId, destinationBankAccountId, transferAmount);
    }
}