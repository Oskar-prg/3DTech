const formElement = document.querySelector("#loginForm")
formElement.setAttribute('novalidate', 'true')

formElement.addEventListener('submit', function (event) {
    if (formElement.checkValidity()) {
        console.log("Success")
        formElement.submit()
    } else {
        console.log("Prevent default")
        event.preventDefault()
    }
})


let errors = []
const entries = formElement.elements
console.log(entries.length)
for (let i = 0; i < entries.length; i++) {
    let validInput = entries[i].nodeName.match('INPUT')
    if (entries[i].willValidate && validInput) {
        console.log(entries[i])
        entries[i].addEventListener('input', reportError)
    }
}

function isEmail(param) {
    return /^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$/.test(param)
}

function isPsswd(param) {
    return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[._-])[A-Za-z\d._-]{8,16}/.test(param)
}

const email = document.getElementsByName("fieldEmail")[0]
const psswd = document.getElementsByName("fieldPassword")[0]

function reportError() {
    const alertElement = document.getElementById('alert-box')
    const emailMsg = 'Email non valida'
    const psswdMsg = 'Password non valida, composizione: almeno una lettera maiuscola,' +
        ' almeno una minuscola, almeno un numero ed un carattere speciale (.-_)'

    if (!isEmail(email.value)) {
        email.setCustomValidity('Email invalid')
        if (errors.indexOf(emailMsg) === -1)
            errors.push(emailMsg)
    } else {
        email.setCustomValidity("")
        errors.splice(errors.indexOf(emailMsg), 1)
        console.log("email valid")
    }

    if (!isPsswd(psswd.value)) {
        console.log("password not valid")
        psswd.setCustomValidity("Password invalid")
        if (errors.indexOf(psswdMsg) === -1)
            errors.push(psswdMsg)
    } else {
        psswd.setCustomValidity("")
        errors.splice(errors.indexOf(psswdMsg), 1)
        console.log("password valid")
    }

    if (errors.length > 0) {
        console.log(errors)
        alertElement.style.display = "block"
        alertElement.className = "alert"
        alertElement.innerHTML = errors.join("<br>")
    } else {
        alertElement.style.display = "none"
        alertElement.innerHTML = ""
    }
}