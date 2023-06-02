// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import {
  LanguageClient,
  LanguageClientOptions,
  ServerOptions,
} from "vscode-languageclient/node";
import * as vscode from "vscode";

//TODO: Read jar from env
const serverOptions: ServerOptions = {
  command:
    "/Users/heli.jerez/Library/Java/JavaVirtualMachines/corretto-17.0.7/Contents/Home/bin/java",
  args: [
    "-jar",
    "/Users/heli.jerez/dev/ksharp/ksharp-kt/lsp/build/libs/ks-lsp-all.jar",
  ],
};

const clientOptions: LanguageClientOptions = {
  documentSelector: [
    // Active functionality on files of these languages.
    {
      language: "ksharp",
    },
  ],
};

let client: LanguageClient;

// This method is called when your extension is activated
// Your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {
  // Create the language client and start the client.
  client = new LanguageClient(
    "KSharpLanguageServer",
    "KSharp Language Server",
    serverOptions,
    clientOptions
  );

  // Start the client. This will also launch the server
  client.start();
}

// This method is called when your extension is deactivated
export function deactivate() {
  if (!client) {
    return undefined;
  }
  return client.stop();
}
