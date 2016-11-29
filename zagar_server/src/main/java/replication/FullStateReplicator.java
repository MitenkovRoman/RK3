package replication;

import main.ApplicationContext;
import matchmaker.MatchMaker;
import model.GameSession;
import model.Player;
import model.PlayerCell;
import model.Virus;
import network.ClientConnections;
import network.packets.PacketReplicate;
import org.eclipse.jetty.websocket.api.Session;
import protocol.model.Cell;
import protocol.model.Food;

import java.io.IOException;
import java.util.Map;

/**
 * @author Alpi
 * @since 31.10.16
 */
public class FullStateReplicator implements Replicator {
  @Override
  public void replicate() {
    for (GameSession gameSession : ApplicationContext.instance().get(MatchMaker.class).getActiveGameSessions()) {
      Food[] food = new Food[gameSession.getField().getFoods().size()];
      int i = 0;
      for(model.Food elem : gameSession.getField().getFoods()){
          food[i] = new Food(elem.getX(), elem.getY());
          i++;
      }

      int numberOfCellsInSession = 0;
      for (Player player : gameSession.getPlayers()) {
        numberOfCellsInSession += player.getCells().size();
      }
      Cell[] cells = new Cell[numberOfCellsInSession];
      i = 0;
      for (Player player : gameSession.getPlayers()) {
        for (PlayerCell playerCell : player.getCells()) {
          cells[i] = new Cell(playerCell.getId(), player.getId(), false, playerCell.getMass(), playerCell.getX(), playerCell.getY());
          i++;
        }
      }
      for (Virus virus: gameSession.getField().getViruses()) {
          cells[i] = new Cell(-1, -1, true, virus.getMass(), virus.getX(), virus.getY());
          i++;
      }
      for (Map.Entry<Player, Session> connection : ApplicationContext.instance().get(ClientConnections.class).getConnections()) {
        if (gameSession.getPlayers().contains(connection.getKey())) {
          try {
            new PacketReplicate(cells, food).write(connection.getValue());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }

    /*ApplicationContext.instance().get(MatchMaker.class).getActiveGameSessions().stream().flatMap(
        gameSession -> gameSession.getPlayers().stream().flatMap(
            player -> player.getCells().stream()
        )
    ).map(playerCell -> new Cell(playerCell.getId(), ))*/
  }
}