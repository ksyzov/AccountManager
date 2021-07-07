package me.ksyz.accountmanager;

public class GuiNotification {
  private final long duration = 5000;
  private String msg = "";
  private int color = 0;
  private long startTime = 0;

  public void setNotification(String msg, int color) {
    this.msg = msg;
    this.color = color;
    this.startTime = System.currentTimeMillis();
  }

  public String getNotificationText() {
    long timeLeft = System.currentTimeMillis() - startTime;
    if (timeLeft <= duration) {
      return "[" + (((duration - timeLeft) / 100) / 10.0) + "s] " + msg;
    }

    return "";
  }

  public int getColor() {
    return this.color;
  }
}
