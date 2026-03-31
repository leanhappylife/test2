const postUrl = `${String(base).replace(/\/+$/, '')}/rest/api/content`
const resp = await fetch(postUrl, { method: 'POST', headers, body: JSON.stringify(body) })

node tools/confluence-create-page.js --title "API Child Page 002" --spaceKey EFOS --storage "<p>Created under parent page.</p>" --ancestorId 1864386183 --instance alm --pretty

node tools/confluence-create-page.js ^
  --title "My new page" ^
  --spaceKey EFOS ^
  --storage-file body.html ^
  --ancestorId 1864386183 ^
  --instance alm ^
  --pretty



====================
  $token = "YOUR_PAT_TOKEN"

$headers = @{
  Authorization = "Bearer $token"
  "Content-Type" = "application/json"
  Accept = "application/json"
}

$body = @{
  type = "page"
  title = "Test child page"
  space = @{
    key = "EFOS"
  }
  ancestors = @(
    @{ id = 1864386183 }
  )
  body = @{
    storage = @{
      value = "<p>This is a child page.</p>"
      representation = "storage"
    }
  }
} | ConvertTo-Json -Depth 10

Invoke-RestMethod `
  -Uri "https://alm-confluence.systems.uk.hsbc/confluence/rest/api/content" `
  -Method Post `
  -Headers $headers `
  -Body $body



========
  {
  "type": "page",
  "title": "Test child page",
  "space": {
    "key": "EFOS"
  },
  "ancestors": [
    {
      "id": 1864386183
    }
  ],
  "body": {
    "storage": {
      "value": "<p>This is a child page.</p>",
      "representation": "storage"
    }
  }
}



------------
  curl -i -X POST "https://alm-confluence.systems.uk.hsbc/confluence/rest/api/content" ^
-H "Authorization: Bearer YOUR_PAT_TOKEN" ^
-H "Content-Type: application/json" ^
-H "Accept: application/json" ^
--data @create-page.json

  
