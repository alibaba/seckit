package com.alibaba.seckit.jdbc.parser.oracle;


import com.alibaba.seckit.jdbc.JdbcURLUnsafeException;

public class NVFactory {
    public NVFactory() {
    }

    public NVPair createNVPair(String str) throws JdbcURLUnsafeException {
        NVTokens tokens = new NVTokens();
        tokens.parseTokens(str);
        return this._readTopLevelNVPair(tokens);
    }

    private NVPair _readTopLevelNVPair(NVTokens token) throws JdbcURLUnsafeException {
        int tk = token.getToken();
        token.eatToken();
        if (tk != 1) {
            Object[] var5 = new Object[]{"(", this.getContext(token)};
            throw new JdbcURLUnsafeException("SyntaxError-04602");
        } else {
            String var3 = this._readNVLiteral(token);
            NVPair var4 = new NVPair(var3);
            if ((tk = token.getToken()) != 3) {
                return this._readRightHandSide(var4, token);
            } else {
                while(tk == 8 || tk == 3) {
                    var3 = var3 + token.popLiteral();
                    tk = token.getToken();
                }

                var4.setName(var3);
                return this._readRightHandSide(var4, token);
            }
        }
    }

    private NVPair _readNVPair(NVTokens var1) throws JdbcURLUnsafeException {
        int var2 = var1.getToken();
        var1.eatToken();
        if (var2 != 1 && var2 != 3) {
            Object[] var5 = new Object[]{"( or ,", this.getContext(var1)};
            throw new JdbcURLUnsafeException("SyntaxError-04602");
        } else {
            String var3 = this._readNVLiteral(var1);
            NVPair var4 = new NVPair(var3);
            return this._readRightHandSide(var4, var1);
        }
    }

    private NVPair _readRightHandSide(NVPair var1, NVTokens var2) throws JdbcURLUnsafeException {
        int var3;
        Object[] var5;
        switch(var2.getToken()) {
            case 2:
            case 3:
                var1.setAtom(var1.getName());
                break;
            case 4:
                var2.eatToken();
                var3 = var2.getToken();
                if (var3 == 8) {
                    String var4 = this._readNVLiteral(var2);
                    var1.setAtom(var4);
                } else {
                    this._readNVList(var2, var1);
                }
                break;
            default:
                var5 = new Object[]{"=", this.getContext(var2)};
                throw new JdbcURLUnsafeException("SyntaxError-04602");
        }

        var3 = var2.getToken();
        if (var3 == 2) {
            var2.eatToken();
        } else if (var3 != 3) {
            var5 = new Object[]{var2.getLiteral(), this.getContext(var2)};
            throw new JdbcURLUnsafeException("UnexpectedChar-04605");
        }

        return var1;
    }

    private String _readNVLiteral(NVTokens var1) throws JdbcURLUnsafeException {
        int var2 = var1.getToken();
        if (var2 != 8) {
            Object[] var3 = new Object[]{"LITERAL", this.getContext(var1)};
            throw new JdbcURLUnsafeException("SyntaxError-04602");
        } else {
            return var1.popLiteral();
        }
    }

    private void _readNVList(NVTokens var1, NVPair var2) throws JdbcURLUnsafeException {
        int var3 = var1.getToken();
        if (var3 == 1 || var3 == 3) {
            NVPair var4 = this._readNVPair(var1);
            var2.addListElement(var4);
            if ((var3 == 3 || var4.getName() == var4.getAtom()) && var2.getListType() != NVPair.LIST_COMMASEP) {
                var2.setListType(NVPair.LIST_COMMASEP);
            }

            this._readNVList(var1, var2);
        }
    }

    private String getContext(NVTokens var1) throws JdbcURLUnsafeException {
        return " " + var1.popLiteral() + " " + var1.popLiteral() + " " + var1.popLiteral();
    }
}
