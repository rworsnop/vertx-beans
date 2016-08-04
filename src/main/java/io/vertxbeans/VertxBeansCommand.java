package io.vertxbeans;

import io.vertx.core.impl.launcher.commands.BareCommand;

/**
 * Created by manishk on 8/4/16.
 */
public class VertxBeansCommand extends BareCommand {

    protected String getHostDefaultAddress() {
        return getDefaultAddress();
    }
}
