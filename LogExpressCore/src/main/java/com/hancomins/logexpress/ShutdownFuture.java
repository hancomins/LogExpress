package com.hancomins.logexpress;

/**
 * Interface representing a future that can be shut down.<br>
 * ShutdownFuture 인터페이스는 종료될 수 있는 Future를 나타냅니다.
 */
public interface ShutdownFuture {

    /**
     * Checks if the shutdown process has ended.<br>
     * 종료 프로세스가 끝났는지 확인합니다.
     *
     * @return true if the shutdown process has ended, false otherwise.<br>
     *         종료 프로세스가 끝났다면 true, 그렇지 않다면 false를 반환합니다.
     */
    public boolean isEnd();

    /**
     * Called when the shutdown process ends. <br>Does nothing when called by the user.<br>
     * 종료 프로세스가 끝날 때 호출됩니다. <br>사용자에 의해 호출시 아무 동작도 하지 않습니다.
     */
    public void onEnd();

    /**
     * Sets a callback to be executed when the shutdown process ends.<br>
     * 종료 프로세스가 끝날 때 실행될 콜백을 설정합니다.
     *
     * @param runnable the callback to be executed.<br>
     *                 실행될 콜백.
     */
    public void addOnEndCallback(Runnable runnable);

    /**
     * Waits for the shutdown process to end.<br>
     * 종료 프로세스가 끝날 때까지 대기합니다.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting.<br>
     *                              현재 스레드가 대기 중에 인터럽트되면 발생합니다.
     */
    public void await() throws InterruptedException;

}