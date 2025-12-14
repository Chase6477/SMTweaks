(function(username, password) {

document.querySelector('input[autocomplete="username"]').value = username;
document.querySelector('input[autocomplete="username"]').dispatchEvent(new Event('input', { bubbles: true }));
document.querySelector('input[autocomplete="current-password"]').value = password;
document.querySelector('input[autocomplete="current-password"]').dispatchEvent(new Event('input', { bubbles: true }));
document.querySelector('button.btn.btn-primary.float-right').click();
})