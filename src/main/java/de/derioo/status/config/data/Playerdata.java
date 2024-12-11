package de.derioo.status.config.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.derioo.status.Main;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.naming.Name;
import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Jacksonized
@Builder
public class Playerdata {

    @JsonProperty("status")
    @Builder.Default
    private List<Status> statuses = new ArrayList<>();

    public static Playerdata load(Main plugin) {
        File dir = new File("plugins/Status");
        File file = new File(dir,  "data.json");
        dir.mkdirs();
        try {
            if (file.createNewFile()) {
                return new Playerdata(new ArrayList<>());
            }
            return new ObjectMapper().readValue(file, Playerdata.class);
        } catch (IOException e) {
            return new Playerdata(new ArrayList<>());
        }
    }



    @JsonIgnore
    public List<String> getStatusNamesList() {
        return this.statuses.stream().map(Status::getName).toList();
    }

    public void save() {
        File dir = new File("plugins/Status");
        File file = new File(dir,  "data.json");
        dir.mkdirs();
        try {
            new ObjectMapper().writeValue(file, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Status getStatusByOwner(Player player) {
        return this.statuses.stream().filter(status -> status.getOwner().equals(player.getUniqueId().toString())).findFirst().orElse(null);
    }
    public Status getStatusByOwner(UUID owner) {
        return this.statuses.stream().filter(status -> status.getOwner().equals(owner.toString())).findFirst().orElse(null);
    }
    public Status getStatusbyMember(Player member) {
        return this.statuses.stream().filter(status -> status.getMembers().contains(member.getUniqueId().toString())).findFirst().orElse(null);
    }

    public void setStatus(Player player, @NotNull String name) {
        TextDisplay armorStand = player.getWorld().spawn(player.getLocation().add(0, 2, 0), TextDisplay.class, (stand) -> {
            stand.text(MiniMessage.miniMessage().deserialize(name));
            stand.setBillboard(Display.Billboard.CENTER);
            stand.getPersistentDataContainer().set(new NamespacedKey("status", "custom"), PersistentDataType.BOOLEAN, true);
        });
        ArmorStand temp = player.getWorld().spawn(player.getLocation().add(0, 2, 0), ArmorStand.class, (stand) -> {
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setCanMove(false);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.getPersistentDataContainer().set(new NamespacedKey("status", "custom"), PersistentDataType.BOOLEAN, true);
        });
        temp.addPassenger(armorStand);
        player.addPassenger(temp);
    }

    public void reloadStatus() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            for (Entity passenger : player.getPassengers()) {
                for (Entity passengerPassenger : passenger.getPassengers()) {
                    passengerPassenger.remove();
                }
            }
            player.getPassengers().forEach(Entity::remove);
        }
        this.statuses.forEach(status -> {
            status.getMembers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                if (player != null) {
                    setStatus(player, status.getName());
                }
            });
        });
    }
}
