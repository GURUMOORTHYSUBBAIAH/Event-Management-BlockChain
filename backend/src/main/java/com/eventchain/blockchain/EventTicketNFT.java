package com.eventchain.blockchain;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EventTicketNFT {
    private final String contractAddress;
    private final Web3j web3j;
    private final TransactionManager transactionManager;
    private final ContractGasProvider gasProvider;

    public EventTicketNFT(String contractAddress, Web3j web3j, Credentials credentials, long chainId) {
        this(contractAddress, web3j, new RawTransactionManager(web3j, credentials, chainId), new DefaultGasProvider());
    }

    public EventTicketNFT(String contractAddress, Web3j web3j, TransactionManager transactionManager,
                          ContractGasProvider gasProvider) {
        this.contractAddress = contractAddress;
        this.web3j = web3j;
        this.transactionManager = transactionManager;
        this.gasProvider = gasProvider;
    }

    public TransactionReceipt mintTicket(String to, BigInteger eventId, String metadataUri) throws Exception {
        Function function = new Function("mintTicket",
                Arrays.asList(new Address(to), new Uint256(eventId), new Utf8String(metadataUri)),
                Collections.singletonList(new TypeReference<Uint256>() {}));
        String encodedFunction = FunctionEncoder.encode(function);
        EthSendTransaction response = transactionManager.sendTransaction(
                gasProvider.getGasPrice("mintTicket"),
                gasProvider.getGasLimit("mintTicket"), contractAddress, encodedFunction, BigInteger.ZERO);
        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }
        return waitForReceipt(response.getTransactionHash());
    }

    public TransactionReceipt markAttendance(BigInteger tokenId) throws Exception {
        Function function = new Function("markAttendance",
                Collections.singletonList(new Uint256(tokenId)),
                Collections.emptyList());
        String encodedFunction = FunctionEncoder.encode(function);
        var response = transactionManager.sendTransaction(gasProvider.getGasPrice("markAttendance"),
                gasProvider.getGasLimit("markAttendance"), contractAddress, encodedFunction, BigInteger.ZERO);
        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }
        return waitForReceipt(response.getTransactionHash());
    }

    public TransactionReceipt issueCertificateHash(BigInteger tokenId, byte[] certHash) throws Exception {
        Bytes32 hash = new Bytes32(certHash.length == 32 ? certHash : Arrays.copyOf(certHash, 32));
        Function function = new Function("issueCertificateHash",
                Arrays.asList(new Uint256(tokenId), hash),
                Collections.emptyList());
        String encodedFunction = FunctionEncoder.encode(function);
        var response = transactionManager.sendTransaction(gasProvider.getGasPrice("issueCertificateHash"),
                gasProvider.getGasLimit("issueCertificateHash"), contractAddress, encodedFunction, BigInteger.ZERO);
        if (response.hasError()) {
            throw new RuntimeException(response.getError().getMessage());
        }
        return waitForReceipt(response.getTransactionHash());
    }

    public String ownerOf(BigInteger tokenId) throws Exception {
        Function function = new Function("ownerOf",
                Collections.singletonList(new Uint256(tokenId)),
                Collections.singletonList(new TypeReference<Address>() {}));
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(org.web3j.protocol.core.methods.request.Transaction
                .createEthCallTransaction(null, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST)
                .send();
        List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        return result.isEmpty() ? null : ((Address) result.get(0)).getValue();
    }

    public byte[] getCertificateHash(BigInteger tokenId) throws Exception {
        Function function = new Function("getCertificateHash",
                Collections.singletonList(new Uint256(tokenId)),
                Collections.singletonList(new TypeReference<Bytes32>() {}));
        String encodedFunction = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(org.web3j.protocol.core.methods.request.Transaction
                .createEthCallTransaction(null, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST)
                .send();
        List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        return result.isEmpty() ? null : ((Bytes32) result.get(0)).getValue();
    }

    private TransactionReceipt waitForReceipt(String txHash) throws Exception {
        for (int i = 0; i < 50; i++) {
            EthGetTransactionReceipt resp = web3j.ethGetTransactionReceipt(txHash).send();
            if (resp.getTransactionReceipt().isPresent()) {
                return resp.getTransactionReceipt().get();
            }
            Thread.sleep(1000);
        }
        throw new RuntimeException("Transaction receipt not found");
    }

    public static EventTicketNFT load(String contractAddress, Web3j web3j, Credentials credentials, long chainId) {
        return new EventTicketNFT(contractAddress, web3j, credentials, chainId);
    }
}
