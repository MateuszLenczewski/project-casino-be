package pl.casino.be.dto;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    @DocumentId
    String uid;
    String email;
    String displayName;
    BigDecimal balance;
}