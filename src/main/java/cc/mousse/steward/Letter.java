package cc.mousse.steward;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author MochaMousse
 */
@Data
@AllArgsConstructor
public class Letter {
  private String server;
  private String player;
  private String content;
}
