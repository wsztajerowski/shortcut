package pl.symentis.shorturl.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import pl.symentis.shorturl.domain.Account;

@Component
public interface AccountRepository extends MongoRepository<Account, String>, CustomizedAccountRepository {

}
