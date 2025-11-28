package com.myorg.trading.service.broker;

import com.myorg.trading.domain.entity.BrokerAccount;
import com.myorg.trading.domain.repository.BrokerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Thin service around BrokerAccountRepository that encrypts credentials before saving.
 */
@Service
public class BrokerAccountService {

    private final BrokerAccountRepository repository;
    private final CredentialManager credentialManager;

    public BrokerAccountService(BrokerAccountRepository repository, CredentialManager credentialManager) {
        this.repository = repository;
        this.credentialManager = credentialManager;
    }

    public List<BrokerAccount> listAccountsForUser(Long userId) {
        return repository.findByUserId(userId);
    }

    public Optional<BrokerAccount> findForUserAndBroker(Long userId, String brokerId) {
        return repository.findByUserIdAndBrokerId(userId, brokerId);
    }

    @Transactional
    public BrokerAccount saveEncryptedCredentials(BrokerAccount account, String credentialsPlainJson) {
        String encrypted = credentialManager.encrypt(credentialsPlainJson);
        account.setCredentialsEncrypted(encrypted);
        return repository.save(account);
    }

    @Transactional
    public BrokerAccount updateMetadata(Long accountId, String metadataJson) {
        BrokerAccount ba = repository.findById(accountId).orElseThrow();
        ba.setMetadataJson(metadataJson);
        return repository.save(ba);
    }

    public Optional<String> readDecryptedCredentials(Long accountId) {
        return repository.findById(accountId).map(acc -> {
            String enc = acc.getCredentialsEncrypted();
            if (enc == null) return null;
            return credentialManager.decrypt(enc);
        });
    }

    @Transactional
    public void delete(Long accountId) {
        repository.deleteById(accountId);
    }
}
