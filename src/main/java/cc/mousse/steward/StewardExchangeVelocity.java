package cc.mousse.steward;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

/**
 * @author MochaMousse
 */
@Slf4j
@Plugin(id = "steward-exchange-velocity", name = "steward-exchange-velocity", version = "2025.1.20")
public class StewardExchangeVelocity {
  private static final String AI_URL = "http://127.0.0.1:1237/mc";
  private static final MinecraftChannelIdentifier IDENTIFIER =
      MinecraftChannelIdentifier.from("steward:exchange");
  private static final OkHttpClient CLIENT =
      new OkHttpClient()
          .newBuilder()
          .connectTimeout(10, TimeUnit.MINUTES)
          .readTimeout(10, TimeUnit.MINUTES)
          .writeTimeout(10, TimeUnit.MINUTES)
          .callTimeout(10, TimeUnit.MINUTES)
          .build();
  private static final Gson GSON = new Gson();
  private final ProxyServer proxyServer;

  @Inject
  public StewardExchangeVelocity(ProxyServer proxyServer) {
    this.proxyServer = proxyServer;
    proxyServer.getChannelRegistrar().register(IDENTIFIER);
  }

  @Subscribe
  public void onPluginMessage(PluginMessageEvent event) {
    proxyServer
        .getScheduler()
        .buildTask(
            this,
            () -> {
              if (Objects.equals(event.getIdentifier(), IDENTIFIER)) {
                broadcast(event.getData());
                String data = new String(event.getData(), StandardCharsets.UTF_8);
                Letter letter = GSON.fromJson(data, Letter.class);
                if (letter.ai == 1) {
                  Request request =
                      new Request.Builder()
                          .url(AI_URL)
                          .post(
                              RequestBody.create(
                                  data, MediaType.get("application/json; charset=utf-8")))
                          .build();
                  try (Response response = CLIENT.newCall(request).execute()) {
                    String message;
                    if (response.isSuccessful()) {
                      message =
                          GSON.fromJson(
                                  Objects.requireNonNull(response.body()).string(),
                                  ResponseData.class)
                              .data;
                    } else {
                      message = "服务暂时不可用";
                    }
                    broadcast(new Letter(-1, letter.server, letter.player, message));
                  } catch (IOException e) {
                    log.warn("无法调用AI", e);
                    broadcast(new Letter(-1, letter.server, letter.player, "服务暂时不可用"));
                  }
                }
              }
            })
        .schedule();
  }

  private void broadcast(Letter letter) {
    broadcast(GSON.toJson(letter).getBytes(StandardCharsets.UTF_8));
  }

  private void broadcast(byte[] letter) {
    proxyServer
        .getAllServers()
        .forEach(registeredServer -> registeredServer.sendPluginMessage(IDENTIFIER, letter));
  }

  @Data
  @AllArgsConstructor
  public static class Letter {
    private Integer ai;
    private String server;
    private String player;
    private String content;
  }

  public record ResponseData(String data) {}
}
