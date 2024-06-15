//
//  QuizView.swift
//  QuizApp
//
//  Created by Jinde Zou on 6/13/22.
//  Copyright © 2022 QuizApp. All rights reserved.
//

import SwiftUI
import WebKit

struct QuizView: UIViewRepresentable {

    private var mWKWebView = WKWebView()
    private var mDelegate = Delegate()

    func makeUIView(context: Context) -> WKWebView {
        mWKWebView.uiDelegate = mDelegate
        return mWKWebView
    }

    func updateUIView(_ webView: WKWebView, context: Context) {
        print("updateUIView : \(webView)")
        /* http://quizwebapp-env.eba-dhiyby8z.us-east-2.elasticbeanstalk.com/sign-in.jsp
        let url = URL(string:"https://www.google.com")
        let request = URLRequest(url:url!)
        webView.load(request)
         */
    }

    // load url
    func load(gitUrl:URL) {
        var request = URLRequest(url: gitUrl)
        request.httpMethod = "GET"
        let task = URLSession.shared.dataTask(with: request) { (data, response, error) in
            if let err = error {
                print("init error : \(err)")
                return
            }
            if let resp = response as? HTTPURLResponse {
                print("init response : \(resp.statusCode)")
            }
            if let data = data, let urlStr = String(data:data, encoding: .utf8) {
                print("init url : \(urlStr)")
                if let url = URL(string:urlStr)  {
                    // USE `weakSelf?` INSTEAD OF `self` to avoid build error
                    let request = URLRequest(url:url)
                    //UIApplication.shared.open(url, options:[:], completionHandler:(nil))
                    DispatchQueue.main.async {
                        mWKWebView.load(request)
                    }
                }
            }
        }
        task.resume()
    }
}

//https://github.com/LINGLemon/LXFSwiftApp/tree/master/LXFSwiftApp/Pages/WKWebView
class Delegate : NSObject, WKUIDelegate{
    // show js alert
    func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage msg: String,
                 initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
        showDialog(msg: msg, msgHandler: completionHandler)
    }

    // show dialog : only has the close button
    func showDialog(msg: String, msgHandler: @escaping () -> Void) {
        if let rootVC = UIApplication.shared.keyWindow?.rootViewController {
            let alert = UIAlertController.init(title: "", message: msg, preferredStyle: .alert)
            let action = UIAlertAction.init(title: "close", style: .default) { (action) in
                msgHandler()
            }
            alert.addAction(action)
            //self.present(alert, animated: true, completion: nil)
            rootVC.present(alert, animated: true)
        }
    }

    // show Yes or No dialog
    func showYesOrNo(msg: String, msgHandler: @escaping (Bool) -> Void) {
        if let rootVC = UIApplication.shared.keyWindow?.rootViewController {
            let dialog = UIAlertController(title: "", message: msg, preferredStyle: .alert)
            let noAction = UIAlertAction(title: "No", style: .cancel) { _ in
                msgHandler(false)
            }
            let okAction = UIAlertAction(title: "Yes", style: .default) { _ in
                msgHandler(true)
            }
            dialog.addAction(noAction)
            dialog.addAction(okAction)
            //self.present(dialog, animated: true, completion: nil)
            rootVC.present(dialog, animated: true)
        }
    }

    // show JS confirm dialog
    func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String,
                 initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
        showYesOrNo(msg: message, msgHandler: completionHandler)
    }

}
