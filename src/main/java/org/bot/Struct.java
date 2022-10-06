package org.bot;

public class Struct {
    private final String m_data;
    private final boolean m_exit;

    Struct(String data) {
        m_data = data;
        m_exit = false;
    }

    Struct(String data, boolean exit) {
        m_data = data;
        m_exit = exit;
    }

    public String getData() {
        return m_data;
    }

    public boolean getExit() {
        return m_exit;
    }
}
