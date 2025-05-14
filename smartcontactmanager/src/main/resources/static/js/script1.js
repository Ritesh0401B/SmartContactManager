function toggleVisibility(toggleId, inputId) {

				const toggle = document.getElementById(toggleId);

				const input = document.getElementById(inputId);

				if (toggle && input) {
					toggle.addEventListener("click", () => {


						if (input.type === "password") {
							input.type = "text"; // show password

							toggle.classList.add("fa-eye");
							toggle.classList.remove("fa-eye-slash");

						} else {
							input.type = "password";

							toggle.classList.add("fa-eye-slash");
							toggle.classList.remove("fa-eye");
						}

					});
				}

			}

			document.addEventListener("DOMContentLoaded", () => {
				toggleVisibility("toggleOld", "oldPassword");
				toggleVisibility("toggleNew", "newPassword");
				toggleVisibility("toggleConfirm", "confirmPassword");
				
				toggleVisibility("toggleLogin", "loginPassword");
				
				toggleVisibility("toggleSignup", "signupPassword")
			});

