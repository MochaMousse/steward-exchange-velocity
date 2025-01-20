package cc.mousse.steward;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import java.nio.charset.StandardCharsets;

/**
 * @author MochaMousse
 */
@Plugin(id = "steward-exchange-velocity", name = "steward-exchange-velocity", version = "2025.1.20")
public class StewardExchangeVelocity {
  private static final MinecraftChannelIdentifier IDENTIFIER =
      MinecraftChannelIdentifier.from("steward:exchange");
  private final ProxyServer proxyServer;

  @Inject
  public StewardExchangeVelocity(ProxyServer proxyServer) {
    this.proxyServer = proxyServer;
    proxyServer.getChannelRegistrar().register(IDENTIFIER);
  }

  @Subscribe
  public void onPlayChat(PlayerChatEvent event) {
    event
        .getPlayer()
        .getCurrentServer()
        .ifPresent(
            serverConnection -> {
              String server = serverConnection.getServerInfo().getName();
              String player = event.getPlayer().getUsername();
              String content = event.getMessage();
              proxyServer
                  .getAllServers()
                  .forEach(
                      registeredServer ->
                          registeredServer.sendPluginMessage(
                              IDENTIFIER,
                              new Gson()
                                  .toJson(new Letter(server, player, content))
                                  .getBytes(StandardCharsets.UTF_8)));
            });
  }
}
