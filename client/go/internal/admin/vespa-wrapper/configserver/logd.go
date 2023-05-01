// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
// Author: arnej

package configserver

import (
	"os"

	"github.com/vespa-engine/vespa/client/go/internal/admin/envvars"
	"github.com/vespa-engine/vespa/client/go/internal/admin/trace"
	"github.com/vespa-engine/vespa/client/go/internal/util"
)

func maybeStartLogd() {
	if os.Getenv(envvars.VESPA_CONFIGSERVER_MULTITENANT) == "true" {
		backticks := util.BackTicksForwardStderr
		out, err := backticks.Run("libexec/vespa/start-logd")
		if err != nil {
			panic(err)
		}
		trace.Info(out)
	}
}
