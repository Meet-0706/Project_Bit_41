const PDFDocument = require('pdfkit');
const fs = require('fs');

const doc = new PDFDocument();
doc.pipe(fs.createWriteStream('C:/Users/meetp/.gemini/antigravity/brain/941f0ab7-3305-4fb4-9bda-c101c97445c6/How_Websites_Work.pdf'));

doc.fontSize(20).text('How Modern Websites Work (The Restaurant Analogy)', { align: 'center' });
doc.moveDown();

doc.fontSize(16).text('1. How do you put a website on the internet?');
doc.fontSize(12).text(`Right now, building a website on your own laptop is like setting up a restaurant in your own living room. It works perfectly, but only you can eat there because no one else can get into your house.

To let everyday people access it, you do the following:

- Rent a Building (Cloud Server): You rent a computer from a company like Amazon, Google, or DigitalOcean. This computer lives in a massive data center, is left turned on 24/7, and is connected to the public internet.
- Move in your Furniture (Deploy Code): You copy your website's code onto that rented computer and turn it on.
- Put up a Signboard (Domain Name): Computers use IP addresses (like 192.168.1.5), which are hard to remember. So, you buy a domain name (like www.mycoolsite.com), which acts as your easy-to-read street address. You link the name to your rented computer. Now, anyone in the world can type in that name and walk into your "restaurant."`);
doc.moveDown();

doc.fontSize(16).text('2. How is the data kept safe from regular users?');
doc.fontSize(12).text(`If people are visiting your website, why can't they just steal your data or mess with your servers? Because of how a modern website is divided into three parts:

- The Frontend (The Dining Room & Menu): This is the visual website that loads in the user's browser. Customers are only allowed to sit in the dining room, look at the menu, and click buttons.
- The Backend (The Waiter): Customers are absolutely not allowed to walk into the kitchen and grab food themselves. If they want something, they must ask the Waiter (the Backend API). The Waiter takes their order, checks if it's a valid request, and goes to the back.
- The Database (The Locked Kitchen/Pantry): This is where all your raw data (passwords, credit cards, user information) is stored. It is hidden deep behind locked doors. It is not connected to the internet. The only person allowed in the kitchen is the Waiter.

So, how do YOU check the data?
Because you are the Restaurant Owner. You don't use the front door like a normal customer. You have a special, encrypted master key (called an SSH key) that unlocks a secret back door to the server. You can walk straight into the kitchen, open the pantry (the database), and look at all the raw data whenever you want, while the normal customers remain safely fenced off in the dining room!`);

doc.end();
console.log('PDF Generated successfully!');
