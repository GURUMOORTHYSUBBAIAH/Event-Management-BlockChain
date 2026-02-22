package com.eventchain.blockchain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.HexFormat;

@Service
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true")
public class NftContractService {
    private final EventTicketNFT contract;
    private final Web3j web3j;

    public NftContractService(
            @Value("${blockchain.contract-address}") String contractAddress,
            @Value("${blockchain.private-key}") String privateKey,
            @Value("${blockchain.rpc-url}") String rpcUrl,
            @Value("${blockchain.chain-id:80001}") long chainId) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(privateKey);
        this.contract = EventTicketNFT.load(contractAddress, web3j, credentials, chainId);
    }

    public MintResult mintTicket(String toAddress, long eventId, String metadataUri) throws Exception {
        TransactionReceipt receipt = contract.mintTicket(toAddress, BigInteger.valueOf(eventId), metadataUri);
        long tokenId = extractTokenIdFromReceipt(receipt);
        return new MintResult(tokenId, receipt.getTransactionHash());
    }

    public void markAttendance(long tokenId) throws Exception {
        contract.markAttendance(BigInteger.valueOf(tokenId));
    }

    public void issueCertificateHash(long tokenId, byte[] certHash) throws Exception {
        byte[] hash32 = certHash.length >= 32 ? java.util.Arrays.copyOf(certHash, 32) : java.util.Arrays.copyOf(certHash, 32);
        contract.issueCertificateHash(BigInteger.valueOf(tokenId), hash32);
    }

    public boolean verifyOwnership(String ownerAddress, long tokenId) throws Exception {
        String owner = contract.ownerOf(BigInteger.valueOf(tokenId));
        return owner != null && owner.equalsIgnoreCase(ownerAddress);
    }

    public byte[] getCertificateHash(long tokenId) throws Exception {
        return contract.getCertificateHash(BigInteger.valueOf(tokenId));
    }

    public record MintResult(long tokenId, String transactionHash) {}

    private long extractTokenIdFromReceipt(TransactionReceipt receipt) {
        if (receipt.getLogs() == null || receipt.getLogs().isEmpty()) return 0;
        var log = receipt.getLogs().stream()
                .filter(l -> l.getTopics().size() >= 3)
                .findFirst();
        if (log.isEmpty()) return 0;
        String topic = log.get().getTopics().get(2);
        return new BigInteger(topic.substring(2), 16).longValueExact();
    }
}
