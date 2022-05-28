package me.ksyz.accountmanager.utils;

public class Notification {
  private static String msg = "";
  private static int color = -1;
  private static long startTime = -1L;

  public static final long DURATION = 5000L;

  public static String getNotificationText() {
    final long elapsedTime = System.currentTimeMillis() - startTime;
    if (DURATION >= elapsedTime) {
      return String.format("[%.1fs] %s", ((DURATION - elapsedTime) / 100L) / 10.0F, msg);
    }
    return "";
  }

  public static void setNotification(final String msg, final int color) {
    Notification.msg = msg;
    Notification.color = color;
    Notification.startTime = System.currentTimeMillis();
  }

  public static void resetNotification() {
    Notification.msg = "";
    Notification.color = -1;
    Notification.startTime = -1L;
  }

  public static int getColor() {
    return color;
  }
}
