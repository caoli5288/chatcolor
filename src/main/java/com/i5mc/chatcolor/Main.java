package com.i5mc.chatcolor;

import com.avaje.ebean.EbeanServer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import lombok.SneakyThrows;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Main extends JavaPlugin {

    private static final Cache<UUID, Bean> POOL = CacheBuilder.newBuilder().build();
    private static EbeanServer database;

    private EZPlaceholderHook hook;

    interface IFunc {

        String apply(CommandSender p, Iterator<String> itr);
    }

    enum Label {

        HAD(Main::had),
        NOW(Main::now);

        private final IFunc func;

        Label(IFunc func) {
            this.func = func;
        }
    }

    @SneakyThrows
    static String had(CommandSender p, Iterator<String> itr) {
        try {
            ColorMapping mapping = ColorMapping.valueOf(join(itr));
            int all = getBean((Player) p).getAllBuy();
            int v = mapping.getValue();
            return String.valueOf((all & v) == v);
        } catch (IllegalArgumentException ign) {
        }
        return "false";
    }

    static String join(Iterator itr) {
        StringBuilder b = new StringBuilder();
        while (itr.hasNext()) {
            b.append(itr.next());
            if (itr.hasNext()) {
                b.append('_');
            }
        }
        return b.toString();
    }

    @SneakyThrows
    static String now(CommandSender p, Iterator<String> itr) {
        int hold = getBean((Player) p).getHold();
        return hold == 0 ? "" : ColorMapping.BY_VALUE.get(hold).getChatColor().toString();
    }

    @SneakyThrows
    static Bean getBean(Player p) {
        return POOL.get(p.getUniqueId(), () -> {
            Bean bean = database.find(Bean.class, p.getUniqueId());
            if (bean == null) {
                bean = database.createEntityBean(Bean.class);
                bean.setId(p.getUniqueId());
            }
            return bean;
        });
    }

    private class MyHook extends EZPlaceholderHook {

        public MyHook() {
            super(Main.this, "i5chatcolor");
        }

        public String onPlaceholderRequest(Player p, String label) {
            Iterator<String> itr = Arrays.asList(label.toUpperCase().split("_")).iterator();
            try {
                return Label.valueOf(itr.next()).func.apply(p, itr);
            } catch (IllegalArgumentException ign) {
            }
            return "null";
        }
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
        if (db.isNotInitialized()) {
            db.define(Bean.class);
            db.initialize();
            db.install();
        }
        database = db.getServer();
        hook = new MyHook();
        hook.hook();

        PluginHelper.addExecutor(this, "i5chatcolor", "i5chatcolor.admin", this::execute);
    }

    enum Sub {

        ADD((p, itr) -> {
            Player player = Bukkit.getPlayerExact(itr.next());
            if (player == null) {
                p.sendMessage("玩家不在线");
                return null;
            }
            ColorMapping mapping = ColorMapping.valueOf(itr.next().toUpperCase());
            Bean bean = getBean(player);
            if ((bean.getAllBuy() & mapping.getValue()) == mapping.getValue()) {
                p.sendMessage("玩家已拥有颜色");
                return null;
            }
            bean.setAllBuy(bean.getAllBuy() + mapping.getValue());
            save(bean);
            p.sendMessage("okay");
            return null;
        }),

        ALL((p, itr) -> {
            if (itr.hasNext()) {
                Player player = Bukkit.getPlayerExact(itr.next());
                if (player == null) {
                    p.sendMessage("玩家不在线");
                    return null;
                }
                Bean bean = getBean(player);
                List<ColorMapping> all = ColorMapping.all(bean.getAllBuy());
                if (all.isEmpty()) {
                    p.sendMessage("玩家不拥有颜色");
                    return null;

                }
                for (ColorMapping mapping : all) {
                    p.sendMessage(mapping.getChatColor() + mapping.name());
                }
            } else {
                for (ColorMapping mapping : ColorMapping.values()) {
                    p.sendMessage(mapping.getChatColor() + mapping.name());
                }
            }
            return null;
        }),

        SET((p, itr) -> {
            Player player = Bukkit.getPlayerExact(itr.next());
            if (player == null) {
                p.sendMessage("玩家不在线");
                return null;
            }
            ColorMapping mapping = ColorMapping.valueOf(itr.next().toUpperCase());
            Bean bean = getBean(player);
            if ((bean.getAllBuy() & mapping.getValue()) == mapping.getValue()) {
                bean.setHold(mapping.getValue());
                save(bean);
                p.sendMessage("okay");
                return null;
            }
            p.sendMessage("玩家没有此颜色");
            return null;
        });

        private final IFunc func;

        Sub(IFunc func) {
            this.func = func;
        }
    }

    static void save(Bean bean) {
        CompletableFuture.runAsync(() -> database.save(bean));
    }

    void execute(CommandSender who, List<String> input) {
        if (input.isEmpty()) {
            who.sendMessage("/i5chatcolor set <player> <color>");
            who.sendMessage("/i5chatcolor add <player> <color>");
            who.sendMessage("/i5chatcolor all");
            who.sendMessage("/i5chatcolor all <player>");
        } else {
            Iterator<String> itr = input.iterator();
            Sub.valueOf(itr.next().toUpperCase()).func.apply(who, itr);
        }
    }
}
