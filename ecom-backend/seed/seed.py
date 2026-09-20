#!/usr/bin/env python3
"""
seed.py — Seeds categories and products into the IBM Bookstore API.
Run: python ecom-backend/seed/seed.py
"""
import json, urllib.request, urllib.error, sys

BASE = "http://172.27.50.14:8080"

def call(method, path, body=None, token=None):
    url = BASE + path
    data = json.dumps(body).encode() if body else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as r:
            return json.loads(r.read())
    except urllib.error.HTTPError as e:
        return json.loads(e.read())

# ── 1. Register admin ──────────────────────────────────────────
print("Registering admin user...")
r = call("POST", "/api/v1/auth/register", {
    "username": "admin", "email": "admin@ibmbookstore.com",
    "password": "Admin@12345", "fullName": "Store Admin", "phone": "5550001111"
})
if r.get("success"):
    token = r["data"]["token"]
    print("  Registered OK")
else:
    # Already registered — login instead
    print("  Already exists, logging in...")
    r = call("POST", "/api/v1/auth/login", {
        "email": "admin@ibmbookstore.com", "password": "Admin@12345"
    })
    if not r.get("success"):
        print("  Login failed:", r.get("message")); sys.exit(1)
    token = r["data"]["token"]
    print("  Logged in OK")

# ── 2. Categories ──────────────────────────────────────────────
categories = [
    {"name": "Fiction",             "description": "Novels, short stories and literary fiction"},
    {"name": "Science & Technology","description": "Programming, engineering and science"},
    {"name": "Business",            "description": "Business, finance and entrepreneurship"},
    {"name": "Self-Help",           "description": "Personal growth, productivity and wellness"},
    {"name": "History",             "description": "World history, biographies and memoirs"},
]
cat_ids = {}
print("\nCreating categories...")
for c in categories:
    r = call("POST", "/api/v1/categories", c, token)
    if r.get("success"):
        cat_ids[c["name"]] = r["data"]["id"]
        print(f"  Created: {c['name']} (id={r['data']['id']})")
    else:
        print(f"  Note: {c['name']} — {r.get('message')}")

# Always refresh category list from API
cats = call("GET", "/api/v1/categories")
cat_ids = {c["name"]: c["id"] for c in (cats.get("data") or [])}
print("  Category IDs:", cat_ids)

# ── 3. Products ────────────────────────────────────────────────
def cid(name):
    for k,v in cat_ids.items():
        if name.lower() in k.lower():
            return v
    return None

products = [
    # Fiction
    {"title":"The Great Gatsby","author":"F. Scott Fitzgerald","isbn":"978-0-7432-7356-5",
     "description":"A story of wealth, love and the American Dream set in the Jazz Age.",
     "price":12.99,"originalPrice":16.99,"stockQuantity":42,
     "publisher":"Scribner","publicationYear":1925,"tentativeDeliveryDays":3,
     "averageRating":4.5,"reviewCount":2840,"categoryId":cid("Fiction"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg"},
    {"title":"To Kill a Mockingbird","author":"Harper Lee","isbn":"978-0-06-112008-4",
     "description":"A Pulitzer Prize-winning masterwork of honor and injustice in the deep South.",
     "price":13.99,"originalPrice":17.99,"stockQuantity":35,
     "publisher":"HarperCollins","publicationYear":1960,"tentativeDeliveryDays":3,
     "averageRating":4.8,"reviewCount":5120,"categoryId":cid("Fiction"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780061120084-L.jpg"},
    {"title":"1984","author":"George Orwell","isbn":"978-0-452-28423-4",
     "description":"A dystopian social science fiction novel and cautionary tale about totalitarianism.",
     "price":10.99,"originalPrice":14.99,"stockQuantity":60,
     "publisher":"Signet Classic","publicationYear":1949,"tentativeDeliveryDays":3,
     "averageRating":4.7,"reviewCount":6300,"categoryId":cid("Fiction"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg"},
    {"title":"The Alchemist","author":"Paulo Coelho","isbn":"978-0-06-231500-7",
     "description":"A magical story about following your dreams and listening to your heart.",
     "price":11.99,"originalPrice":15.99,"stockQuantity":50,
     "publisher":"HarperOne","publicationYear":1988,"tentativeDeliveryDays":4,
     "averageRating":4.6,"reviewCount":4200,"categoryId":cid("Fiction"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780062315007-L.jpg"},
    # Science & Technology
    {"title":"Clean Code","author":"Robert C. Martin","isbn":"978-0-13-235088-4",
     "description":"A handbook of agile software craftsmanship. The definitive guide to writing clean code.",
     "price":34.99,"originalPrice":44.99,"stockQuantity":28,
     "publisher":"Prentice Hall","publicationYear":2008,"tentativeDeliveryDays":2,
     "averageRating":4.7,"reviewCount":3100,"categoryId":cid("Science"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg"},
    {"title":"The Pragmatic Programmer","author":"Andrew Hunt & David Thomas","isbn":"978-0-13-595705-9",
     "description":"Your journey to mastery. From Journeyman to Master.",
     "price":39.99,"originalPrice":49.99,"stockQuantity":22,
     "publisher":"Addison-Wesley","publicationYear":2019,"tentativeDeliveryDays":2,
     "averageRating":4.8,"reviewCount":2800,"categoryId":cid("Science"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780135957059-L.jpg"},
    {"title":"Designing Data-Intensive Applications","author":"Martin Kleppmann","isbn":"978-1-4920-3289-0",
     "description":"The big ideas behind reliable, scalable, and maintainable systems.",
     "price":42.99,"originalPrice":55.99,"stockQuantity":18,
     "publisher":"O'Reilly Media","publicationYear":2017,"tentativeDeliveryDays":3,
     "averageRating":4.9,"reviewCount":1950,"categoryId":cid("Science"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9781492032908-L.jpg"},
    {"title":"A Brief History of Time","author":"Stephen Hawking","isbn":"978-0-553-38016-3",
     "description":"From the Big Bang to black holes — Hawking explores the universe.",
     "price":14.99,"originalPrice":18.99,"stockQuantity":45,
     "publisher":"Bantam Books","publicationYear":1988,"tentativeDeliveryDays":3,
     "averageRating":4.6,"reviewCount":4800,"categoryId":cid("Science"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780553380163-L.jpg"},
    # Business
    {"title":"The Lean Startup","author":"Eric Ries","isbn":"978-0-307-88791-7",
     "description":"How continuous innovation creates radically successful businesses.",
     "price":18.99,"originalPrice":24.99,"stockQuantity":32,
     "publisher":"Crown Business","publicationYear":2011,"tentativeDeliveryDays":3,
     "averageRating":4.5,"reviewCount":3400,"categoryId":cid("Business"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780307887917-L.jpg"},
    {"title":"Zero to One","author":"Peter Thiel","isbn":"978-0-8041-3929-8",
     "description":"Notes on startups, or how to build the future.",
     "price":16.99,"originalPrice":22.99,"stockQuantity":40,
     "publisher":"Crown Business","publicationYear":2014,"tentativeDeliveryDays":3,
     "averageRating":4.4,"reviewCount":2700,"categoryId":cid("Business"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780804139298-L.jpg"},
    {"title":"Good to Great","author":"Jim Collins","isbn":"978-0-06-662099-2",
     "description":"Why some companies make the leap and others don't.",
     "price":17.99,"originalPrice":23.99,"stockQuantity":25,
     "publisher":"HarperBusiness","publicationYear":2001,"tentativeDeliveryDays":4,
     "averageRating":4.6,"reviewCount":3200,"categoryId":cid("Business"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780066620992-L.jpg"},
    # Self-Help
    {"title":"Atomic Habits","author":"James Clear","isbn":"978-0-7352-1129-9",
     "description":"Tiny changes, remarkable results. The definitive guide to building good habits.",
     "price":17.99,"originalPrice":22.99,"stockQuantity":55,
     "publisher":"Avery","publicationYear":2018,"tentativeDeliveryDays":2,
     "averageRating":4.9,"reviewCount":8400,"categoryId":cid("Self"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780735211292-L.jpg"},
    {"title":"Thinking, Fast and Slow","author":"Daniel Kahneman","isbn":"978-0-374-53355-7",
     "description":"A fascinating exploration of the two systems that drive the way we think.",
     "price":16.99,"originalPrice":20.99,"stockQuantity":30,
     "publisher":"Farrar Straus Giroux","publicationYear":2011,"tentativeDeliveryDays":3,
     "averageRating":4.6,"reviewCount":5100,"categoryId":cid("Self"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780374533557-L.jpg"},
    {"title":"The 7 Habits of Highly Effective People","author":"Stephen R. Covey","isbn":"978-1-9821-8238-3",
     "description":"Powerful lessons in personal change. A timeless classic.",
     "price":15.99,"originalPrice":19.99,"stockQuantity":48,
     "publisher":"Simon & Schuster","publicationYear":1989,"tentativeDeliveryDays":3,
     "averageRating":4.7,"reviewCount":6200,"categoryId":cid("Self"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9781982182380-L.jpg"},
    # History
    {"title":"Sapiens: A Brief History of Humankind","author":"Yuval Noah Harari","isbn":"978-0-06-231609-7",
     "description":"How Homo sapiens came to dominate the planet and what it means for our future.",
     "price":18.99,"originalPrice":24.99,"stockQuantity":38,
     "publisher":"Harper","publicationYear":2015,"tentativeDeliveryDays":3,
     "averageRating":4.8,"reviewCount":9200,"categoryId":cid("History"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg"},
    {"title":"Educated","author":"Tara Westover","isbn":"978-0-399-59050-4",
     "description":"A memoir about a young girl who grows up in a survivalist family and goes on to earn a PhD.",
     "price":14.99,"originalPrice":18.99,"stockQuantity":43,
     "publisher":"Random House","publicationYear":2018,"tentativeDeliveryDays":3,
     "averageRating":4.8,"reviewCount":7300,"categoryId":cid("History"),
     "imageUrl":"https://covers.openlibrary.org/b/isbn/9780399590504-L.jpg"},
]

print(f"\nSeeding {len(products)} products...")
ok, fail = 0, 0
for p in products:
    if not p.get("categoryId"):
        print(f"  Skipping (no cat): {p['title']}")
        fail += 1
        continue
    r = call("POST", "/api/v1/products", p, token)
    if r.get("success"):
        print(f"  OK {p['title']}")
        ok += 1
    else:
        print(f"  FAIL {p['title']}: {r.get('message')}")
        fail += 1

print(f"\nDone: {ok} created, {fail} failed.")
print("\nYou can now browse the store at: http://localhost:3000")
