package com.alibaba.seckit.jdbc.parser.oracle;

import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;

public final class NVPair {
    public static int RHS_NONE = 0;
    public static int RHS_ATOM = 1;
    public static int RHS_LIST = 2;
    public static int LIST_REGULAR = 3;
    public static int LIST_COMMASEP = 4;
    private String _name;
    private int _rhsType;
    private String _atom;
    private Vector _list;
    private int _listType;
    private NVPair _parent;
    static final String LINE_SEPARATOR = getLineSeparatorProperty();

    public NVPair(String var1) {
        this._name = var1;
        this._atom = null;
        this._list = null;
        this._listType = LIST_REGULAR;
        this._parent = null;
        this._rhsType = RHS_NONE;
    }

    public NVPair(String var1, String var2) throws JdbcURLUnsafeException {
        this(var1);
        this.setAtom(var2);
    }

    public NVPair(String var1, NVPair var2) {
        this(var1);
        this.addListElement(var2);
    }

    public String getName() {
        return this._name;
    }

    public void setName(String var1) {
        this._name = var1;
    }

    public NVPair getParent() {
        return this._parent;
    }

    private void _setParent(NVPair var1) {
        this._parent = var1;
    }

    public int getRHSType() {
        return this._rhsType;
    }

    public int getListType() {
        return this._listType;
    }

    public void setListType(int var1) {
        this._listType = var1;
    }

    public String getAtom() {
        return this._atom;
    }

    public void setAtom(String var1) throws JdbcURLUnsafeException {
        if (!this._name.contains("COMMENT") && this.containsComment(var1)) {
            Object[] var2 = new Object[]{"#", this.getName()};
            throw new JdbcURLUnsafeException("UnexpectedChar-04603");
        } else {
            this._rhsType = RHS_ATOM;
            this._atom = var1;
            this._list = null;
        }
    }

    private boolean containsComment(String var1) {
        for(int var2 = 0; var2 < var1.length(); ++var2) {
            if (var1.charAt(var2) == '#') {
                if (var2 == 0) {
                    return true;
                }

                if (var1.charAt(var2 - 1) != '\\') {
                    return true;
                }
            }
        }

        return false;
    }

    public int getListSize() {
        return this._list == null ? 0 : this._list.size();
    }

    public NVPair getListElement(int var1) {
        return this._list == null ? null : (NVPair)this._list.elementAt(var1);
    }

    public void addListElement(NVPair var1) {
        if (this._list == null) {
            this._rhsType = RHS_LIST;
            this._list = new Vector(3, 5);
            this._atom = null;
        }

        this._list.addElement(var1);
        var1._setParent(this);
    }

    public void removeListElement(int var1) {
        if (this._list != null) {
            this._list.removeElementAt(var1);
            if (this.getListSize() == 0) {
                this._list = null;
                this._rhsType = RHS_NONE;
            }
        }

    }

    private String space(int var1) {
        String var2 = new String("");

        for(int var3 = 0; var3 < var1; ++var3) {
            var2 = var2 + " ";
        }

        return var2;
    }

    public String trimValueToString() {
        String var3 = this.valueToString().trim();
        return var3.substring(1, var3.length() - 1);
    }

    public String valueToString() {
        String var1 = "";
        if (this._rhsType == RHS_ATOM) {
            var1 = var1 + this._atom;
        } else if (this._rhsType == RHS_LIST) {
            int var2;
            if (this._listType == LIST_REGULAR) {
                for(var2 = 0; var2 < this.getListSize(); ++var2) {
                    var1 = var1 + this.getListElement(var2).toString();
                }
            } else if (this._listType == LIST_COMMASEP) {
                for(var2 = 0; var2 < this.getListSize(); ++var2) {
                    NVPair var3 = this.getListElement(var2);
                    var1 = var1 + var3.getName();
                    if (var2 != this.getListSize() - 1) {
                        var1 = var1 + ", ";
                    }
                }
            }
        }

        return var1;
    }

    public String toString() {
        String var1 = "(" + this._name + "=";
        if (this._rhsType == RHS_ATOM) {
            var1 = var1 + this._atom;
        } else if (this._rhsType == RHS_LIST) {
            int var2;
            if (this._listType == LIST_REGULAR) {
                for(var2 = 0; var2 < this.getListSize(); ++var2) {
                    var1 = var1 + this.getListElement(var2).toString();
                }
            } else if (this._listType == LIST_COMMASEP) {
                var1 = var1 + " (";

                for(var2 = 0; var2 < this.getListSize(); ++var2) {
                    NVPair var3 = this.getListElement(var2);
                    var1 = var1 + var3.getName();
                    if (var2 != this.getListSize() - 1) {
                        var1 = var1 + ", ";
                    }
                }

                var1 = var1 + ")";
            }
        }

        var1 = var1 + ")";
        return var1;
    }

    public String toString(int var1, boolean var2) {
        String var3 = "";
        String var4 = new String(this._name);
        if (this._rhsType == RHS_LIST) {
            if (this._listType == LIST_REGULAR) {
                String var5 = "";

                for(int var6 = 0; var6 < this.getListSize(); ++var6) {
                    if (!var4.equalsIgnoreCase("ADDRESS") && !var4.equalsIgnoreCase("RULE")) {
                        var5 = var5 + this.getListElement(var6).toString(var1 + 1, true);
                    } else {
                        var5 = var5 + this.getListElement(var6).toString(var1 + 1, false);
                    }
                }

                if (!var5.equals("")) {
                    if (!var4.equalsIgnoreCase("ADDRESS") && !var4.equalsIgnoreCase("RULE")) {
                        var3 = var3 + this.space(var1 * 2) + "(" + this._name + " =" + LINE_SEPARATOR;
                    } else {
                        var3 = var3 + this.space(var1 * 2) + "(" + this._name + " = ";
                    }

                    var3 = var3 + var5;
                    if (!var4.equalsIgnoreCase("ADDRESS") && !var4.equalsIgnoreCase("RULE")) {
                        if (var1 == 0) {
                            var3 = var3 + ")";
                        } else if (var1 == 1) {
                            var3 = var3 + this.space(var1 * 2) + ")";
                        } else {
                            var3 = var3 + this.space(var1 * 2) + ")" + LINE_SEPARATOR;
                        }
                    } else {
                        var3 = var3 + ")" + LINE_SEPARATOR;
                    }
                }
            } else if (this._listType == LIST_COMMASEP) {
                var3 = var3 + "(" + this._name + "= (";

                for(int var7 = 0; var7 < this.getListSize(); ++var7) {
                    NVPair var8 = this.getListElement(var7);
                    var3 = var3 + var8.getName();
                    if (var7 != this.getListSize() - 1) {
                        var3 = var3 + ", ";
                    }
                }

                var3 = var3 + "))";
            }
        } else if (this._rhsType == RHS_ATOM) {
            if (var1 == 0) {
                if (var4.indexOf("COMMENT") != -1) {
                    this._atom = this.modifyCommentString(this._atom);
                    var3 = var3 + "(" + this._atom + ")";
                } else {
                    var3 = var3 + "(" + this._name + " = " + this._atom + ")";
                }
            } else if (var4.indexOf("COMMENT") != -1) {
                this._atom = this.modifyCommentString(this._atom);
                var3 = var3 + this._atom + LINE_SEPARATOR;
            } else if (!var2) {
                var3 = var3 + "(" + this._name + " = " + this._atom + ")";
            } else {
                var3 = var3 + this.space(var1 * 2) + "(" + this._name + " = " + this._atom + ")";
                var3 = var3 + LINE_SEPARATOR;
            }
        }

        return var3;
    }

    public String modifyCommentString(String var1) {
        String var2 = "";

        for(int var3 = 0; var3 < var1.length(); var2 = var2 + var1.charAt(var3++)) {
            char var4 = var1.charAt(var3);
            switch(var4) {
                case '\\':
                    if (var1.charAt(var3 + 1) == '(' || var1.charAt(var3 + 1) == '=' || var1.charAt(var3 + 1) == ')' || var1.charAt(var3 + 1) == ',' || var1.charAt(var3 + 1) == '\\') {
                        ++var3;
                    }
            }
        }

        return var2;
    }

    public void println() {
        System.out.println(this.toString());
    }

    public void println(PrintStream var1) {
        if (this._rhsType == RHS_ATOM) {
            var1.println("          (" + this._name + " = " + this._atom + ")");
        } else if (this._rhsType == RHS_LIST) {
            for(int var2 = 0; var2 < this.getListSize(); ++var2) {
                this.getListElement(var2).println(var1);
            }
        }

    }

    private static String getLineSeparatorProperty() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.getProperty("line.separator");
            }

        });
    }
}

