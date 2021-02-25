package modification;

import haven.AuthClient;
import haven.CheckListbox;
import haven.CheckListboxItem;
import haven.Coord;
import haven.GItem;
import haven.Resource;
import haven.UI;
import haven.Utils;
import haven.Widget;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class dev {
    public static boolean logging = Utils.getprefb("msglogging", false);      //allow log in console
    public static boolean loadLog = false;
    public static boolean decodeCode = Utils.getprefb("decodeCode", false);
    public static boolean skipexceptions = Utils.getprefb("skipexceptions", false);
    public static boolean reslog = Utils.getprefb("reslog", false);

    public static boolean msg_log_skip_boolean = Utils.getprefb("skiplogmsg", false);
    public static ArrayList<String> msg_log_skip = new ArrayList<String>() {{       //chosen msg
        addAll(Arrays.asList("glut", "click"));
    }};


    public static CheckListbox msglist = null;
    public final static Map<String, CheckListboxItem> msgmenus = new TreeMap<String, CheckListboxItem>() {{
        Utils.loadcollection("msgcollection").forEach(msg -> put(msg, new CheckListboxItem(msg)));
    }};

    public static void addMsg(String name) {
        List<String> list = new ArrayList<>(msgmenus.keySet());
        if (msgmenus.get(name) == null) {
            list.add(name);
            CheckListboxItem ci = new CheckListboxItem(name);
            msgmenus.put(name, ci);
            if (msglist != null)
                msglist.items.add(ci);
            Utils.setcollection("msgcollection", msgmenus.keySet());
        }
    }

    public static String serverSender = "_SERVER_MSG";
    public static String clientSender = "_CLIENT_MSG";

    public static void sysPrintStackTrace(String text) {
        if (logging) {
            StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
            int stackTraceElementsLength = stackTraceElements.length;

            System.out.print(text + " || ");
            for (int i = 1; i < stackTraceElementsLength; i++) {
                System.out.print("{" + stackTraceElements[i].getClassName() + "(" + stackTraceElements[i].getMethodName() + ")(" + stackTraceElements[i].getLineNumber() + ")");
                if (i != stackTraceElementsLength - 1) System.out.print(">");
            }

            System.out.println();
        }
    }

    public static void resourceLog(Object... strings) {
        if (logging) {
            for (Object s : strings) {
                if (s instanceof Object[]) {
                    argsMethod((Object[]) s);
                } else
                    System.out.print(s == null ? "null" : s.toString() + " ");
            }
            System.out.println();
        }
    }

    public static void sysLog(String who, Widget widget, int id, String msg, Object... args) {
        boolean skip_log = false;
        for (Map.Entry<String, CheckListboxItem> entry : msgmenus.entrySet()) {
            if (msg_log_skip_boolean && entry.getKey().toLowerCase().equals(msg) && entry.getValue().selected)
                skip_log = true;
        }

        if (!skip_log && logging) {
            printStacktrace(who, widget, id);

            int a;
            if (id == -1) a = 1;
            else if ((id / 10) == 0) a = 0;
            else if ((id / 10) < 10) a = 1;
            else a = 2;

            System.out.print(" || " + msg);
            addMsg(msg);
            System.out.print(" || [" + args.length + "]:");


            try {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof AuthClient.NativeCred) {
                        AuthClient.NativeCred arg = (AuthClient.NativeCred) args[i];
                        System.out.print("{(AuthClient.NativeCred):" + arg.name() + "}");
                    } else if (args[i] instanceof Integer) {
                        System.out.print("{i:[" + args[i] + "]}");
                    } else if (args[i] instanceof Long) {
                        System.out.print("{l:[" + args[i] + "]}");
                    } else if (args[i] instanceof String) {
                        System.out.print("{s:[" + args[i] + "]}");
                    } else if (args[i] instanceof Boolean) {
                        System.out.print("{b:[" + args[i] + "]}");
                    } else if (args[i] instanceof Coord) {
                        Coord coord = (Coord) args[i];
                        System.out.print("{x:" + coord.x + ",y:" + coord.y + "}");
                    } else {
                        System.out.print("{" + args[i] + "}");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    private static void printStacktrace(String who, Widget widget, int id) {
        System.out.print("[" + LocalTime.now() + "]");

        System.out.print(" || " + who);
        if (widget.ui != null && widget.ui.sess != null) System.out.print("[" + widget.ui.sess.username + "]");

        System.out.print(" || " + widget + "(" + id + ")");
        if (widget instanceof GItem) {
            try {
                Resource res = ((GItem) widget).getres();
                System.out.print("[" + res + "]");
            } catch (Exception e) {
                System.out.print(e);
            }
        }
    }

    public static void sysLogRemote(String who, Widget widget, int id, String type, int parent, Object[] pargs, Object... cargs) {
        UI ui = null;
        if (widget.ui != null) ui = widget.ui;

        boolean skip_log = false;

        if (!skip_log && logging) {
            printStacktrace(who, widget, id);

            if (type != null) System.out.print(" || " + type);
            if (parent != -1) {
                System.out.print(" || ");
                if (ui != null)
                    System.out.print(widget.ui.widgets.get(parent) + "(");
                System.out.print(parent);
                if (ui != null)
                    System.out.print(")");
            }

            argsMethod(pargs);
            argsMethod(cargs);

            System.out.println();
        }
    }

    private static void argsMethod(Object[] args) {
        if (args != null) {
            System.out.print(" || [" + args.length + "]:");
            for (Object arg : args) {
                System.out.print("{" + args + "}");
            }
        }
    }

    public static void checkFileVersion(String resname, int curver) {
        try {
            Resource res = Resource.remote().loadwait(resname);

            if (res == null) {
                System.out.printf("[i] Resource [%s(%d)] not found!", resname, curver);
                return;
            }
            if (curver != res.ver)
                System.out.printf("[i] Resource [%s] (old %d). Please update!", res, curver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String mapToString(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append("[").append(entry.getKey()).append(",").append(entry.getValue()).append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String collectionToString(Collection<?> collection) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object o : collection) {
            sb.append("[").append(o).append("]");
        }
        sb.append("]");
        return sb.toString();
    }
}