package modification;

import haven.AuthClient;
import haven.Coord;
import haven.GItem;
import haven.Resource;
import haven.UI;
import haven.Utils;
import haven.Widget;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

public class dev {
    public static boolean logging = Utils.getprefb("msglogging", false);      //allow log in console
    public static boolean loadLog = false;
    public static boolean decodeCode = Utils.getprefb("decodeCode", false);
    public static boolean skipexceptions = Utils.getprefb("skipexceptions", false);
    public static boolean reslog = Utils.getprefb("reslog", false);

    public static boolean msg_log_skip_boolean = false;     //allow chosen skip
    public static ArrayList<String> msg_log_skip = new ArrayList<String>() {{       //chosen msg
        addAll(Arrays.asList("glut", "click"));
    }};

    public static String serverSender = "_SERVER_MSG";
    public static String clientSender = "_CLIENT_MSG";
    private static String oldStackTraceElementClass = "";
    private static String oldStackTraceElementMethod = "";
    private static int oldStackTraceElementLine = -1;
    private static String oldSender = "";

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
                System.out.print(s.toString() + " ");
            }
            System.out.println();
        }
    }

    public static void sysLog(String who, Widget widget, int id, String msg, Object... args) {
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        int stackTraceElementsLength = stackTraceElements.length;

        int max_wdg_length = 40;
        int max_msg_length = 10;

        boolean skip_log = false;
        for (String s : msg_log_skip) {
            if (s.equals(msg) && msg_log_skip_boolean) skip_log = true;
        }

        if (stackTraceElements[1].getMethodName().equals("uimsg")) {
            oldStackTraceElementClass = stackTraceElements[2].getClassName();
            oldStackTraceElementMethod = stackTraceElements[2].getMethodName();
            oldStackTraceElementLine = stackTraceElements[2].getLineNumber();
        } else {
            oldStackTraceElementClass = "";
            oldStackTraceElementMethod = "";
            oldStackTraceElementLine = -1;
        }
        oldSender = who;

        if (!skip_log && logging) {
            System.out.print("[" + LocalTime.now() + "]");
            //System.out.print(" || ");
			/*for (int i = 1; i < stackTraceElementsLength; i++) {
				System.out.print("{" + stackTraceElements[i].getClassName() + "(" + stackTraceElements[i].getMethodName() + ")(" + stackTraceElements[i].getLineNumber() + ")");
				if (i != stackTraceElementsLength - 1) System.out.print(">");
			}*/

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

            int a;
            if (id == -1) a = 1;
            else if ((id / 10) == 0) a = 0;
            else if ((id / 10) < 10) a = 1;
            else a = 2;

			/*if (widget != null)
				for (int i = widget.toString().length() + a; i < max_wdg_length; i++)
					System.out.print(" ");
			else
				for (int i = 4 + a; i < max_wdg_length; i++)
					System.out.print(" ");*/
            System.out.print(" || " + msg);
			/*for (int i = msg.length(); i < max_msg_length; i++)
				System.out.print(" ");*/
            System.out.print(" || [" + args.length + "]:");


            try {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof AuthClient.NativeCred) {
                        AuthClient.NativeCred arg = (AuthClient.NativeCred) args[i];
                        System.out.print("{(AuthClient.NativeCred):" + arg.name() + "}");
                    } else if (args[i] instanceof Integer) {
                        System.out.print("i{" + args[i] + "}");
                        if (msg.equals("chres")) System.out.print("[" + widget.ui.sess.getres((Integer) args[i]) + "]");
                    } else if (args[i] instanceof Long) {
                        System.out.print("l{" + args[i] + "}");
                    } else if (args[i] instanceof String) {
                        System.out.print("s{" + args[i] + "}");
                    } else if (args[i] instanceof Boolean) {
                        System.out.print("b{" + args[i] + "}");
                    } else if (args[i] instanceof Coord) {
                        Coord coord = (Coord) args[i];
                        System.out.print("{x:" + coord.x + ", y:" + coord.y + "}");
                    } else {
                        if (stackTraceElements[1].getMethodName().equals("wdgmsg"))
                            System.out.print("{" + args[i].getClass().getName() + ":" + args[i] + "}");
                        else
                            System.out.print("{" + args[i] + "}");
                    }
                    if (i != args.length - 1) System.out.print(", ");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    public static void sysLogRemote(String who, Widget widget, int id, String type, int parent, Object[] pargs, Object... cargs) {
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        int stackTraceElementsLength = stackTraceElements.length;

        UI ui = null;
        if (widget.ui != null) ui = widget.ui;

        boolean skip_log = false;
//        for (String s : msg_log_skip) {
//            if (s.equals(name) && msg_log_skip_boolean) skip_log = true;
//        }

        if (stackTraceElements[1].getMethodName().equals("run") && oldSender.equals(serverSender)) {
            if (oldStackTraceElementClass.equals(stackTraceElements[1].getClassName()) &&
                    oldStackTraceElementMethod.equals(oldStackTraceElementMethod = stackTraceElements[1].getMethodName()) &&
                    oldStackTraceElementLine == stackTraceElements[1].getLineNumber() - 3)
                skip_log = true;
        } else {
            oldStackTraceElementClass = "";
            oldStackTraceElementMethod = "";
            oldStackTraceElementLine = -1;
        }
        oldSender = who;

        if (!skip_log && logging) {
            System.out.print("[" + LocalTime.now() + "]");
//            System.out.print(" || ");
//            for (int i = 1; i < stackTraceElementsLength; i++) {
//                System.out.print("{" + stackTraceElements[i].getClassName() + "(" + stackTraceElements[i].getMethodName() + ")(" + stackTraceElements[i].getLineNumber() + ")");
//                if (i != stackTraceElementsLength - 1) System.out.print(">");
//            }
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

//            if (name != null) System.out.print(" || " + name);
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

    private static void argsMethod(Object[] pargs) {
        if (pargs != null) {
            System.out.print(" || [" + pargs.length + "]:");
            for (int i = 1; i < pargs.length; i++) {
                System.out.print("{" + pargs[i] + "}");
                if (i != pargs.length - 1) System.out.print(", ");
            }
        }
    }

    public static void checkFileVersion(String resname, int curver) {
        Resource res = Resource.remote().loadwait(resname);
        if (res == null) {
            System.out.printf("[i] Resource [%s(%d)] not found!", resname, curver);
            return;
        }
        if (curver != res.ver)
            System.out.printf("[i] Resource [%s] (old %d). Please update!", res, curver);
    }
}
