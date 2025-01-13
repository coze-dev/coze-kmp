import UIKit
import SwiftUI
import ComposeApp

struct ContentView: View {
    var body: some View {
        MainViewControllerRepresentable()
    }
}

struct MainViewControllerRepresentable: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }   
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // 更新时不需要做任何事
    }
}
