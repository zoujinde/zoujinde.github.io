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

    // Read file data
    static func readFile(_ file: String) -> Data {
        let data: Data

        guard let url = Bundle.main.url(forResource: file, withExtension: nil)
        else {
            fatalError("Couldn't find \(file) in main bundle.")
        }

        do {
            data = try Data(contentsOf: url)
        } catch {
            fatalError("Couldn't load \(file) from main bundle:\n\(error)")
        }

        return data
    }

    // Load object from data
    static func load<T: Decodable>(data: Data) -> T {
        do {
            let decoder = JSONDecoder()
            return try decoder.decode(T.self, from: data)
        } catch {
            fatalError("Couldn't parse \(data) as \(T.self):\n\(error)")
        }
    }

    // Load object from file
    static func load<T: Decodable>(file: String) -> T {
        let data = readFile(file)
        return load(data: data)
    }

    // To json string
    static func toJsonStr<T: Encodable>(_ object: T) -> String {
        let jsonEncoder = JSONEncoder()
        let data = try? jsonEncoder.encode(object)
        let str = String(data: data!, encoding: String.Encoding.utf8)
        return str!
    }

}
