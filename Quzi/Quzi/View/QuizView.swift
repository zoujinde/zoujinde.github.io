//
//  QuizForm.swift
//  Quzi
//
//  Created by Jinde Zou on 12/18/21.
//  Copyright © 2021 Jinde Zou. All rights reserved.
//

import SwiftUI

struct QuizView: View {

    private let width = CGFloat(390)
    private let user_name = "Admas"
    private let quiz_name = "Survey of Spanish Media"
    private let quiz_list = QuizList().quiz_list

    //For value type(struct, enum etc), use @State or @Binding for binding
    //For object type, use @StateObject, @ObservedObject, @EnvironmentObject
    @State private var item_index = 0;
    @State private var item_content: String
    @State private var item_answers_array: [String]

    // Init the quiz content and answers
    init() {
        _item_content = State(initialValue: "1. " + quiz_list[0].item_content)
        _item_answers_array = State(initialValue: quiz_list[0].item_answers_array)
    }

    var body: some View {

        //ScrollView {
        VStack {
            Divider()

            Text(quiz_name)
                .frame(width: width)
                .padding(3)

            Divider()

            Text(item_content)
                .frame(width: width, alignment: .leading)
                .font(.system(size: 30))
                .padding(1)
                .foregroundColor(Color.blue)

            Divider()
            
            ForEach(item_answers_array, id: \.self) {str in
                Text(str)
                    .frame(width: self.width, alignment: .leading)
                    .font(.system(size: 30))
                    .border(Color.blue)
                    .padding(5)
            }
            
            Spacer()
            
            HStack {
                Button(action: {self.previous()}, label: {Text("<- Previous")})
                    .frame(width: 120)
                    .padding(3)
                    .border(Color.red)

                Button(action: {self.submit()}, label: {Text("Submit")})
                    .frame(width: 120)
                    .padding(3)
                    .border(Color.red)

                Button(action: {self.next()}, label: {Text("Next ->")})
                    .frame(width: 120)
                    .padding(3)
                    .border(Color.red)

            }
            
            Divider()
            Text("User : \(user_name) \t\t Questions : \(quiz_list.count)")
                .frame(width: width, height: 30)
                .padding(.bottom, 30)
            
        }.font(.system(size: 20))

    }

    //struct is a value type. For value types, only methods explicitly marked as mutating can modify the properties of self.
    //If you change struct to be a class then your code compiles without problems.
    
    // Previous item
    private func previous() {
        if item_index > 0 {
            item_index-=1
            setData(item_index);
        }
    }
    
    // Next item
    private func next() {
        if item_index < quiz_list.count - 1 {
            item_index+=1
            setData(item_index);
        }
    }

    // Set data
    private func setData(_ i: Int) {
        item_content = "\(i + 1). \(quiz_list[i].item_content)"
        item_answers_array = quiz_list[i].item_answers_array
    }
    
    // Submit data
    private func submit() {
    }

}

struct QuizForm_Previews: PreviewProvider {
    static var previews: some View {
        QuizView()
    }
}
