//
//  MyUtil.swift
//  Quzi
//
//  Created by Jinde Zou on 12/20/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import Foundation
import UIKit

/* String trim
extension String {
    func trim() -> String  {
        return self.trimmingCharacters(in: CharacterSet.whitespaces)
    }
}*/

final class MyUtil {

    // Set the rootWindow in SceneDelegate
    static var rootWindow: UIWindow?

    // Show alert
    static func showAlert(_ msg: String) {
        //if let rootVC = UIApplication.shared.keyWindow?.rootViewController {
        if let rootVC = rootWindow?.rootViewController {
            let alert = UIAlertController(title: nil, message:msg, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel))
            rootVC.present(alert, animated: true)
        }
    }

    // String trim
    static func trim(_ str: String) -> String {
        return str.trimmingCharacters(in: CharacterSet.whitespaces)
    }

}
