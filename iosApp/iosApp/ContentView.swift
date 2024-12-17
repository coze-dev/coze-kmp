import UIKit
import SwiftUI
import ComposeApp

struct ContentView: View {
    @ObservedObject private(set) var viewModel: ViewModel
    @State private var inputText: String = "Why is the sky always blue"

    var body: some View {
        VStack {
            HStack {
                TextField("输入内容与 Coze 聊天", text: $inputText)
                    .textFieldStyle(RoundedBorderTextFieldStyle())
                    .padding()

                Button(action: {
                    Task {
                        let text = inputText
                        inputText = ""
                        await viewModel.sendMessage(text)
                    }
                }) {
                    Text("发送")
                }
                .padding()
            }

            ListView(phrases: viewModel.messages)
        }
    }
}

extension ContentView {
    @MainActor
    class ViewModel: ObservableObject {
        @Published var messages: Array<String> = []

        func sendMessage(_ message: String) async {
            messages.append("[User]: " + message + "\n")
            await startObserving(msg: message)
        }

        func startObserving(msg input: String) async {
            guard !input.isEmpty else { return }
            var currentMessage = ""
            for await phrase in ChatDemo().streamTest(msg: input) {
                if currentMessage.isEmpty {
                    currentMessage += phrase
                    self.messages.append("[Coze Bot]: " + currentMessage)
                } else {
                    currentMessage += phrase
                    self.messages[self.messages.count - 1] = currentMessage
                }
            }
        }
    }
}

struct ListView: View {
    let phrases: Array<String>

    var body: some View {
        List(phrases, id: \.self) {
            Text($0)
        }
    }
}
