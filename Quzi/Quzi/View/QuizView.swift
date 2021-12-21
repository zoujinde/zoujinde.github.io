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

    //For value type(struct, enum etc), use @State or @Binding for binding
    //For object type, use @StateObject, @ObservedObject, @EnvironmentObject
    @State private var index = 0;
    @State private var quiz_list = QuizList().quiz_list

    var body: some View {

        //ScrollView {
        VStack {
            Text(quiz_name)
                .frame(width: width)
                .padding(1)

            Divider()

            Text("\(index + 1). \(quiz_list[index].item_content)")
                .frame(width: width, alignment: .leading)
                .font(.system(size: 30))
                .padding(1)
                .foregroundColor(Color.blue)

            Divider()

            ForEach(0 ..< quiz_list[index].array.count, id: \.self) { row in
                Button(action: {self.onClickRow(row)}, label: {AnswerRow(quizItem: self.quiz_list[self.index], row: row)})
                    .frame(width: self.width, alignment: .leading)
                    .font(.system(size: 25))
                    .padding(3)
                    .foregroundColor(Color.black)
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

            Text("User : \(user_name) \t (Questions : \(quiz_list.count))")
                .frame(width: width, height: 30)
                .padding(.bottom, 30)
            
            
        }.font(.system(size: 20))

    }

    //struct is a value type. For value types, only methods explicitly marked as mutating can modify the properties of self.
    //If you change struct to be a class then your code compiles without problems.
    
    // Previous item
    private func previous() {
        if index > 0 {
            index -= 1 // @State value changed on UI
        } else {
            MyUtil.showAlert("To the first item")
        }
    }
    
    // Next item
    private func next() {
        if index < quiz_list.count - 1 {
            index += 1 // @State value changed on UI
        } else {
            MyUtil.showAlert("To the last item")
        }
    }
    
    // Submit data
    private func submit() {
    }

    // On click the answer row
    private func onClickRow(_ rowNum: Int) {
        let rowStr = "\(rowNum)"
        if quiz_list[index].multi_select {
            if quiz_list[index].answer.contains(rowStr) {
                quiz_list[index].answer = quiz_list[index].answer.replacingOccurrences(of: rowStr, with: "")
            } else {
                quiz_list[index].answer += rowStr
            }
        } else { // single select
            quiz_list[index].answer = rowStr
        }
    }

}

struct QuizForm_Previews: PreviewProvider {
    static var previews: some View {
        QuizView()
    }
}
