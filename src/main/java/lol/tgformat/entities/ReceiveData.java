package lol.tgformat.entities;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Getter
public class ReceiveData {
    private String msg;
    private String type;
}
