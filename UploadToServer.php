<?php

	$file_path = "uploads/";

	$file_path = $file_path . basename($_FILES['uploaded_file']['name']);
	if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
		// load the uploaded file
		$file = "uploads/" . basename($_FILES['uploaded_file']['name']);

		$doc_received = new DOMDocument('1.0');
		$doc_received->preserveWhiteSpace = false;
		$doc_received->load($file);
		$doc_received->formatOutput = true;
		// get the entry in the uploaded file
		$node = $doc_received->getElementsByTagName('message')->item(0);

		// load data.xml
		$doc_final = new DOMDocument('1.0');
		$doc_final->preserveWhiteSpace = false;
		$doc_final->load("uploads/data.xml");
		$doc_final->formatOutput = true;

		// import the entry to new document
		$node = $doc_final->importNode($node, true);

		// get root and append the child node
		$root = $doc_final->documentElement;
		$root->appendChild($node);	

		//save file
		$doc_final->save("uploads/data.xml");
		echo "success";
	} else {
		echo "fail";
	}
?>