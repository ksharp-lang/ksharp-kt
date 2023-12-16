// The module 'vscode' contains the VS Code extensibility API
// Import the module and reference it with the alias vscode in your code below
import {LanguageClient, LanguageClientOptions, ServerOptions,} from "vscode-languageclient/node";
import * as vscode from "vscode";

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
    // lsp path
    const serverOptions: ServerOptions = {
        command: vscode.workspace.getConfiguration("ksharp").get("lspServerPath")!,
        args: [],
    };
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
export function deactivate(a: string, b: string) {
    if (!client) {
        return undefined;
    }
    return client.stop();
}
