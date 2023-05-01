package zts

import (
	"testing"

	"github.com/vespa-engine/vespa/client/go/internal/mock"
)

func TestAccessToken(t *testing.T) {
	httpClient := mock.HTTPClient{}
	client, err := NewClient(&httpClient, "vespa.vespa", "http://example.com")
	if err != nil {
		t.Fatal(err)
	}
	httpClient.NextResponseString(400, `{"message": "bad request"}`)
	_, err = client.AccessToken()
	if err == nil {
		t.Fatal("want error for non-ok response status")
	}
	httpClient.NextResponseString(200, `{"access_token": "foo bar"}`)
	token, err := client.AccessToken()
	if err != nil {
		t.Fatal(err)
	}
	want := "foo bar"
	if token != want {
		t.Errorf("got %q, want %q", token, want)
	}
}
