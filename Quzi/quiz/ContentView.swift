//
//  ContentView.swift
//  quiz
//
//  Created by tanli on 2/23/22.
//

import SwiftUI
import WebKit

struct WebView: UIViewRepresentable{
    let url: URL?
    private let webView = WKWebView()
    func makeUIView(context: Context) -> some UIView {
        return webView
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        guard let url = url else {
            return
        }
        webView.load(.init(url: url))
    }
    
}
//typealias UIViewType = <#type#>


struct ContentView: View {
    var body: some View {
        WebView(url: URL(string: "https://www.youtube.com"))
        //Text("Hello, world!")
         //   .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
