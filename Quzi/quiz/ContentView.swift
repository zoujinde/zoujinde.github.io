//
//  ContentView.swift
//  quiz
//
//  Created by tanli on 2/23/22.
//

import SwiftUI
import WebKit

struct WebView: UIViewRepresentable{
    var url: URL?
    private let webView = WKWebView()
    func makeUIView(context: Context) -> some UIView {
        let preferences = WKPreferences()
        preferences.javaScriptEnabled = true

        // Create a configuration for the preferences
        let configuration = WKWebViewConfiguration()
        configuration.preferences = preferences
        //webView.setcon = configuration
        return webView
    }
    
    func updateUIView(_ uiView: UIViewType, context: Context) {
        guard let url = url else {
            return
        }
        webView.navigationDelegate = context.coordinator
        webView.load(.init(url: url))
    }
    
    func loadUrl(_ newUrl: String) {
        guard let newUrl = URL(string: newUrl)else {
            print("Error: cannot create URL")
            return
          }
        webView.load(URLRequest.init(url: newUrl))
    }
    
    func makeCoordinator() -> WebView.Coordinator {
            Coordinator(self)
        }
    
    class Coordinator: NSObject, WKNavigationDelegate {
            let parent: WebView

            init(_ parent: WebView) {
                self.parent = parent
            }
            
        func webView(_ webView: WKWebView, didReceive challenge: URLAuthenticationChallenge, completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
            print("in WKNavigationDelegate didReceive")
            guard let serverTrust = challenge.protectionSpace.serverTrust else {
                completionHandler(.cancelAuthenticationChallenge, nil)
                return
            }
            let exceptions = SecTrustCopyExceptions(serverTrust)
            SecTrustSetExceptions(serverTrust, exceptions)
            completionHandler(.useCredential, URLCredential(trust: serverTrust));
        }

            func webView(_ webView: WKWebView, didCommit navigation: WKNavigation!) {
                //parent.loadStatusChanged?(true, nil)
                print("in WKNavigationDelegate didCommit")
            }

            func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
                //parent.title = webView.title ?? ""
                //parent.loadStatusChanged?(false, nil)
                print("in WKNavigationDelegate didFinish")
            }

            func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
                print("in WKNavigationDelegate didFail");
                //parent.loadStatusChanged?(false, error)
            }
        
        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            if navigationAction.navigationType == .linkActivated {
                guard let url = navigationAction.request.url else {return}
                webView.load(URLRequest(url: url))
            }
            decisionHandler(.allow)
        }
        }
    
    
    
}
//typealias UIViewType = <#type#>


struct ContentView: View {
    @State public var textFieldTxt = "http://www.google.com"
    

    let webView = WebView(url: URL(string: "http://www.google.com"))
    var body: some View {
        
        VStack {
            HStack{
                
                    TextField("ip address", text: $textFieldTxt, onCommit: {
                        redirect(textFieldTxt)
                    })
                    .padding()
                    .background(Color.gray.opacity(0.3).cornerRadius(10))
                    .keyboardType(UIKeyboardType.default)
                    
                Button(action: {
                    loadHomeAddress()
                    print("current textFieldTxt is: " , textFieldTxt)
                    redirect(textFieldTxt)
                    //http://quizwebapp-env.eba-dhiyby8z.us-east-2.elasticbeanstalk.com/sign-in.jsp
                    //webView.loadUrl(textFieldTxt)
                }) {
                    Image(systemName: "magnifyingglass")
                        .font(.largeTitle)
                        .foregroundColor(.red)
                }
                
            }.frame(height:60)
            .background(Color.yellow)
            .cornerRadius(8)
            .padding()
            
            webView
                .onAppear() {
                    //loadHomeAddress()
                }
        }
       
    }
    
    func loadHomeAddress() {
        //self.webView.loadUrl("https://www.youtube.com")
        // Create URL
        //let url = URL(string: "http://127.0.0.1:8080/")
        let url = URL(string: "https://raw.githubusercontent.com/zoujinde/zoujinde.github.io/main/server.txt")
        guard let requestUrl = url else { fatalError() }

        // Create URL Request
        var request = URLRequest(url: requestUrl)

        // Specify HTTP Method to use
        request.httpMethod = "GET"

        // Send HTTP Request
        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
            
            // Check if Error took place
            if let error = error {
                print("Error took place \(error)")
                return
            }
            
            // Read HTTP Response Status code
            if let response = response as? HTTPURLResponse {
                print("Response HTTP Status code: \(response.statusCode)")
            }
            
            // Convert HTTP Response Data to a simple String
            if let data = data, let dataString = String(data: data, encoding: .utf8) {
                print("Response data string:\n \(dataString)")
                textFieldTxt = dataString
            }
            
        }
        task.resume()
    }
    
    func redirect(_ newAddress: String ) {
        self.webView.loadUrl(newAddress)
    }
}



struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
