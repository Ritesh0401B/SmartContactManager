

const toggleSidebar = () => {
	const sidebar = document.getElementById("sidebar");
	const content = document.getElementById("content");

	if (sidebar.style.display === "none") {
		sidebar.style.display = "block";
		content.style.marginLeft = "20%";
	} else {
		sidebar.style.display = "none";
		content.style.marginLeft = "0%";
	}
};


const search = () => {

	console.log("searching.....");

	let queryInput = document.getElementById("search-input");
	let resultBox = document.querySelector(".search-result");

	let query = queryInput.value.trim();

	console.log("User Query:", query); // Debug

	if (query === '') {
		resultBox.style.display = 'none';
		return;
	} else {

		console.log(query);

		let url = `http://localhost:6020/search/${query}`;
		console.log("URL:", url); // Debug

		fetch(url)
			.then((response) => response.json())
			.then((data) => {
				console.log("Fetched Data:", data); // Debug
				let text = `<div class='list-group'>`;

				data.forEach((contact) => {
					console.log("Contact ID:", contact); // यह जोड़ो

					console.log("contact.cId:", contact.cid); // यह जोड़ो

					text += `<a href= '/user/${contact.cid}/contact' class='list-group-item list-group-action'>${contact.name}</a>`;
				});

				text += `</div>`;

				resultBox.innerHTML = text;
				resultBox.style.display = 'block';
			})
			.catch((error) => {
				console.error("Error during fetch:", error);
				// resultBox.innerHTML = "<p>Error loading data</p>";
				resultBox.style.visibility = 'block';
			});
	}
}



// first request- to server to create order

const paymentStart = () => {

	console.log("payment started...");

	const amountField = document.getElementById("payment-field");
	const amountValue = amountField.value.trim();

	console.log(amountValue);

	if (amountValue === '' || amountValue == null) {
		// alert("Amount is required!");

		Swal.fire({
			title: "Amount is required!",
			icon: "error",
			draggable: true
		});

		return;
	}

	// Now using fetch instead of jQuery AJAX
	fetch('/user/create-order', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			amount: amountValue,
			info: 'order_request'
		})
	})
		.then(response => {
			if (!response.ok) {
				throw new Error('Network response was not ok');
			}
			return response.json(); // JSON parse karo
		})
		.then(response => {
			// Server se response mila
			console.log(response);

			if (response.status == 'created') {
				// open payment form

				let option = {
					key: 'rzp_test_fxip5V4ioepJdE',  // Razorpay Key ID
					amount: response.amount,         // Payment amount
					currency: 'INR',                  // Currency type (INR for India)
					name: 'Smart Contact Manager',     // Business name
					description: 'Donation',            // Payment description
					image: 'https://images.unsplash.com/photo-1575936123452-b67c3203c357?fm=jpg&q=60&w=3000&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8aW1hZ2V8ZW58MHx8MHx8fDA%3D',      // Business logo or image   
					order_id: response.id,             // Order ID from server response
					handler: function(response) {       // Payment successful handler
						console.log(response.razorpay_payment_id);
						console.log(response.razorpay_order_id);
						console.log(response.razorpay_signature);

						console.log('payment successful !!');
						// alert("congrates !! Payment successful !")

						updatePaymentOnServer(
							response.razorpay_payment_id,
							response.razorpay_order_id,
							'paid'
						);

						Swal.fire({
							title: "congrates !! Payment successful !",
							icon: "success",
							draggable: true
						});

					},
					"prefill": {            // Prefill user details
						"name": "",
						"email": "",
						"contact": ""
					},
					"notes": {                  // Additional notes
						"address": "Smart Contact Manager "
					},
					"theme": {                  // Custom theme settings
						"color": "#3399cc"
					}

				};

				let rzp = new Razorpay(option);

				rzp.on('payment.failed', function(response) {
					console.log(response.error.code);
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
					// alert("Oops payment failed !!")

					Swal.fire({
						title: "Oops payment failed !!",
						icon: "error",
						draggable: true
					});

					updatePaymentOnServer(
						response.error.metadata.payment_id,
						response.error.metadata.order_id,
						'failed'
					);



				});

				rzp.open();

			}

		})
		.catch(error => {
			console.error('Error:', error);
			alert("Something went wrong!");
		});
};



// 

const updatePaymentOnServer = (payment_id, order_id, status) => {

	fetch("/user/update-order", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify({
			payment_id: payment_id,
			order_id: order_id,
			status: status
		}),
	})
		.then(response => response.json())
		.then(response => {
			// success callback
			console.log("Success:", response);



		})
		.catch(error => {
			// error callback
			console.error("Error:", error);

			Swal.fire({
				title: "Your Payment successful , but we did not get on server , we will contact you as soon as posible",
				icon: "error",
				draggable: true
			});
		});


}





