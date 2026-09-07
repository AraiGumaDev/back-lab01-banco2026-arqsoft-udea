package com.ejemplo.banco.banco2026.service;

import com.ejemplo.banco.banco2026.DTO.TransactionDTO;
import com.ejemplo.banco.banco2026.entity.Customer;
import com.ejemplo.banco.banco2026.entity.Transaction;
import com.ejemplo.banco.banco2026.exception.ResourceNotFoundException;
import com.ejemplo.banco.banco2026.mapper.TransactionMapper;
import com.ejemplo.banco.banco2026.repository.CustomerRepository;
import com.ejemplo.banco.banco2026.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TransactionMapper transactionMapper;

    public TransactionDTO transferMoney(TransactionDTO transactionDTO) {
//validar que los numeros de cuenta no sean nulos
        if(transactionDTO.getSenderAccountNumber()==null ||
                transactionDTO.getReceiverAccountNumber()==null){
            throw new IllegalArgumentException("Sender Account Number or Receiver Account Number cannot be null");
        }
//Buscar los clientes por numero de cuenta
        Customer sender =
                customerRepository.findByAccountNumber(transactionDTO.getSenderAccountNumber())
                        .orElseThrow(()-> new IllegalArgumentException("Sender Account Number not found"));
                                Customer receiver =
                                customerRepository.findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                                        .orElseThrow(()-> new IllegalArgumentException("Receiver Account Number not found"));
//Validar que el remitente tenga saldo suficiente
        if(sender.getBalance() < transactionDTO.getAmount()){
            throw new IllegalArgumentException("Sender Balance not enough");
        }
//realiza la transferencia
        sender.setBalance(sender.getBalance() - transactionDTO.getAmount());
        receiver.setBalance(receiver.getBalance() + transactionDTO.getAmount());
//Guardar los cambios en las cuentas
        customerRepository.save(sender);
        customerRepository.save(receiver);
//Crear y guardar la transaccion
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(sender.getAccountNumber());
        transaction.setReceiverAccountNumber(receiver.getAccountNumber());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction= transactionRepository.save(transaction);
//Devolver la transaccion creada como un DTO
        TransactionDTO savedTransaction = new TransactionDTO();
        savedTransaction.setId(transaction.getId());
        savedTransaction.setSenderAccountNumber(transaction.getSenderAccountNumber());
        savedTransaction.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        savedTransaction.setAmount(transaction.getAmount());
        return savedTransaction;
    }
    public List<TransactionDTO> getTransactionsForAccount(String accountNumber) {
        List<Transaction> transactions =
                transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber(accountNumber,accountNumber);
        return transactions.stream().map(transaction -> {
            TransactionDTO dto = new TransactionDTO();

            dto.setId(transaction.getId());
            dto.setSenderAccountNumber(transaction.getSenderAccountNumber());
            dto.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
            dto.setAmount(transaction.getAmount());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<TransactionDTO> getAlltransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toDTO).toList();
    }

    public TransactionDTO getTransactionById(Long id){
        return transactionRepository.findById(id).map(transactionMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    public TransactionDTO updateTransactionById(Long id, TransactionDTO transactionDTO) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        Customer sender =
                customerRepository.findByAccountNumber(transactionDTO.getSenderAccountNumber())
                        .orElseThrow(()-> new IllegalArgumentException("Sender Account Number not found"));
        Customer receiver =
                customerRepository.findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                        .orElseThrow(()-> new IllegalArgumentException("Receiver Account Number not found"));

        if(!Objects.equals(sender.getAccountNumber(), transaction.getSenderAccountNumber())){
            throw new IllegalArgumentException("The sender's account does not match");
        }
        if(!Objects.equals(receiver.getAccountNumber(), transaction.getReceiverAccountNumber())){
            throw new IllegalArgumentException("The receiver's account does not match");
        }

        if(transactionDTO.getAmount() - transaction.getAmount() < 0){

            //Validar que el remitente tenga saldo suficiente
            if(sender.getBalance() < (transactionDTO.getAmount() - transaction.getAmount())){
                throw new IllegalArgumentException("Sender Balance not enough");
            }
            //realiza la transferencia
            sender.setBalance(sender.getBalance() - (transactionDTO.getAmount() - transaction.getAmount()));
            receiver.setBalance(receiver.getBalance() + (transactionDTO.getAmount() - transaction.getAmount()));
        } else {
            //Validar que sí se le pueda descontar el saldo al receiver
            if(receiver.getBalance() < (transaction.getAmount() - transactionDTO.getAmount())){
                throw new IllegalArgumentException("Receiver Balance not enough");
            }
            //realiza la transferencia
            receiver.setBalance(receiver.getBalance() - (transaction.getAmount() - transactionDTO.getAmount()));
            sender.setBalance(sender.getBalance() + (transaction.getAmount() - transactionDTO.getAmount()));
        }
        //Guardar los cambios en las cuentas
        customerRepository.save(sender);
        customerRepository.save(receiver);

        transactionDTO.setId(transaction.getId());
        transactionDTO.setTimestamp(LocalDateTime.now());
        transaction = transactionMapper.toEntity(transactionDTO);
        transactionRepository.save(transaction);

        return transactionMapper.toDTO(transaction);
    }

    public String deleteTransactionById(Long id) {
        Transaction transaction =  transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Customer sender =
                customerRepository.findByAccountNumber(transaction.getSenderAccountNumber())
                        .orElseThrow(()-> new ResourceNotFoundException("Sender Account Number not found"));
        Customer receiver =
                customerRepository.findByAccountNumber(transaction.getReceiverAccountNumber())
                        .orElseThrow(()-> new ResourceNotFoundException("Receiver Account Number not found"));

        if(receiver.getBalance() < transaction.getAmount()){
            throw new IllegalArgumentException("Receiver Balance not enough");
        }

        sender.setBalance(sender.getBalance() + transaction.getAmount());
        receiver.setBalance(receiver.getBalance() - transaction.getAmount());
        customerRepository.save(sender);
        customerRepository.save(receiver);
        transactionRepository.deleteById(id);

        return String.format("Transaction has been deleted");
    }
}
