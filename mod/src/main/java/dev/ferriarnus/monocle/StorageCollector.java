package dev.ferriarnus.monocle;

import com.google.common.net.InetAddresses;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.taumc.glsl.grammar.GLSLParser;
import org.taumc.glsl.grammar.GLSLParserBaseListener;

import java.util.List;

public class StorageCollector extends GLSLParserBaseListener {

    private final List<TerminalNode> storage;

    public StorageCollector(List<TerminalNode> storage) {
        this.storage = storage;
    }

    @Override
    public void enterStorage_qualifier(GLSLParser.Storage_qualifierContext ctx) {
        if (ctx.getChild(0) instanceof TerminalNode node) {
            this.storage.add(node);
        }
    }
}
