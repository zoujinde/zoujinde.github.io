//
//  QuizData.swift
//  Quzi
//
//  Created by Jinde Zou on 12/19/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import Foundation

// Quiz Data
final class QuizList {
    let quiz_list: [QuizItem] = load("quiz_data.json")
}

// Quiz Item
struct QuizItem: Codable {
    let quiz_id: Int
    let item_id: Int
    let item_content: String
    let item_answers: String
    var array: [String] {
        return item_answers.split(separator: "\n").map{String($0)}
    }
    let multi_select: Bool
    var answer: String
}

// Load data from json file
private func load<T: Decodable>(_ filename: String) -> T {
    let data: Data

    guard let file = Bundle.main.url(forResource: filename, withExtension: nil)
    else {
        fatalError("Couldn't find \(filename) in main bundle.")
    }

    do {
        data = try Data(contentsOf: file)
    } catch {
        fatalError("Couldn't load \(filename) from main bundle:\n\(error)")
    }

    do {
        let decoder = JSONDecoder()
        return try decoder.decode(T.self, from: data)
    } catch {
        fatalError("Couldn't parse \(filename) as \(T.self):\n\(error)")
    }
}
